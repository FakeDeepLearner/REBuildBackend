package com.rebuild.backend.service.user_services;


import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.FriendRelationship;
import com.rebuild.backend.model.entities.profile_entities.ProfilePicture;
import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.users.CaptchaVerificationRecord;
import com.rebuild.backend.model.entities.users.Inbox;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.forms.auth_forms.LoginForm;
import com.rebuild.backend.model.forms.auth_forms.SignupForm;
import com.rebuild.backend.model.forms.dtos.CredentialValidationDTO;
import com.rebuild.backend.model.forms.dtos.forum_dtos.SearchResultDTO;
import com.rebuild.backend.model.forms.resume_forms.ResumeSpecsForm;
import com.rebuild.backend.model.responses.HomePageData;
import com.rebuild.backend.repository.forum_repositories.FriendRelationshipRepository;
import com.rebuild.backend.repository.resume_repositories.ResumeRepository;
import com.rebuild.backend.repository.user_repositories.CaptchaVerificationRepository;
import com.rebuild.backend.repository.user_repositories.ProfilePictureRepository;
import com.rebuild.backend.repository.user_repositories.ProfileRepository;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import com.rebuild.backend.service.token_services.OTPService;
import com.rebuild.backend.service.util_services.ElasticSearchService;
import com.sendgrid.SendGrid;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.cdimascio.dotenv.Dotenv;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class UserService{

    private final int SALT_BYTE_LENGTH = 16;

    private final UserRepository repository;

    private final PasswordEncoder encoder;

    private final SessionRegistry sessionRegistry;

    private final ResumeRepository resumeRepository;

    private final ProxyManager<String> proxyManager;
    private final BucketConfiguration bucketConfiguration;

    private final FriendRelationshipRepository friendRelationshipRepository;

    private final CaptchaVerificationRepository verificationRepository;

    private final OTPService otpService;

    private final ProfileService profileService;

    private final Dotenv dotenv;

    private final ElasticSearchService elasticSearchService;


    @Autowired
    public UserService(UserRepository repository,
                       SessionRegistry sessionRegistry,
                       ResumeRepository resumeRepository,
                       ProxyManager<String> proxyManager,
                       BucketConfiguration bucketConfiguration,
                       FriendRelationshipRepository friendRelationshipRepository,
                       CaptchaVerificationRepository verificationRepository,
                       OTPService otpService, ProfileService profileService,
                       Dotenv dotenv, ElasticSearchService elasticSearchService) {
        this.repository = repository;
        this.verificationRepository = verificationRepository;
        this.otpService = otpService;
        this.profileService = profileService;
        this.dotenv = dotenv;
        this.elasticSearchService = elasticSearchService;
        this.encoder = new BCryptPasswordEncoder();
        this.sessionRegistry = sessionRegistry;
        this.resumeRepository = resumeRepository;
        this.proxyManager = proxyManager;
        this.bucketConfiguration = bucketConfiguration;
        this.friendRelationshipRepository = friendRelationshipRepository;
    }

    public void invalidateAllSessions(String username){
        List<SessionInformation> allSessions = sessionRegistry.getAllSessions(username, false);
        if (allSessions != null) {
            allSessions.forEach(SessionInformation::expireNow);
        }
    }

    public Optional<User> findByEmailOrPhone(String emailOrPhone)
    {
        if(emailOrPhone.contains("@"))
        {
            return findByEmail(emailOrPhone);
        }
        else {
            return repository.findByPhoneNumber(emailOrPhone);
        }
    }

    public Optional<User> findByEmail(String email){
        return repository.findByEmail(email);
    }


    @Transactional
    public void changePassword(User changingUser, String newRawPassword){
        String userSalt = changingUser.getSaltValue();
        String pepper = dotenv.get("PEPPER_VALUE");
        String newHashedPassword = encoder.encode(newRawPassword + userSalt + pepper);

        changingUser.setPassword(newHashedPassword);
        repository.save(changingUser);
    }

    @Transactional
    public void changeEmail(User changingUser, String newEmail){
        try {
            changingUser.setEmail(newEmail);
        }
        catch (DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            if (cause instanceof ConstraintViolationException violationException){
                if (Objects.equals(violationException.getConstraintName(), "uk_email")){
                    throw new RuntimeException("This email address already exists");
                }
            }
        }
    }

    public boolean captchaFailed(String userResponse, String userIp)
    {
        String urlToPost = "https://www.google.com/recaptcha/api/siteverify";

        Map<String, String> body = new HashMap<>();
        body.put("secret", dotenv.get("GOOGLE_CAPTCHA_SECRET_KEY"));
        body.put("response", userResponse);
        body.put("remoteip", userIp);

        RequestEntity<Map<String, String>> request = RequestEntity.post(urlToPost).body(body);

        ResponseEntity<Map> response = new RestTemplate().exchange(request, Map.class);

        Map<String, String> result = response.getBody();

        if(result == null){
            return true;
        }
        String successString = result.get("success");
        String timestampString = result.get("challenge_ts");
        ZonedDateTime timestamp = ZonedDateTime.parse(timestampString,
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZZ"));

        boolean success = Boolean.parseBoolean(successString);

        CaptchaVerificationRecord newRecord = new CaptchaVerificationRecord(userIp,  timestamp, success);
        verificationRepository.save(newRecord);

        return !success;


    }

    public CredentialValidationDTO validateLoginCredentials(LoginForm form) {
        String formField = form.emailOrPhone();

        User foundUser = findByEmailOrPhone(formField).orElse(null);

        if (foundUser == null) {
            return new CredentialValidationDTO(false, "whatever", "whatever", false);
        }


        String userChannel;
        if(formField.contains("@"))
        {
            userChannel = "email";
        }

        else
        {
            userChannel = "sms";
        }

        try {
            new AccountStatusUserDetailsChecker().check(foundUser);
        }
        catch (AccountExpiredException expiredException)
        {
            otpService.generateOTPCode(formField, userChannel);
            return new CredentialValidationDTO(false,
                    "whatever", "whatever", true);
        }


        String userSalt = foundUser.getSaltValue();
        String pepper = dotenv.get("PEPPER_VALUE");

        return new CredentialValidationDTO(encoder.matches(form.password() + userSalt + pepper,
                foundUser.getPassword()), foundUser.getEmail(), userChannel, false);

    }

    @Transactional
    public void removePhoneOf(User deletingUser){
        deletingUser.setPhoneNumber(null);
        save(deletingUser);
    }

    private String generateSaltValue(){
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_BYTE_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    @Transactional
    public User createNewUser(SignupForm signupForm, MultipartFile pictureFile) throws IOException {
        String generatedSalt = generateSaltValue();
        String pepper = dotenv.get("PEPPER_VALUE");
        String encodedPassword = encoder.encode(signupForm.password() + generatedSalt + pepper);
        ZoneId userTimeZone = ZoneId.of(signupForm.timezoneAsString());
        User newUser = new User(encodedPassword, signupForm.email(),
                signupForm.phoneNumber(), signupForm.forumUsername(), generatedSalt, userTimeZone);

        UserProfile newUserProfile = createNewProfile(newUser, pictureFile);
        newUser.setProfile(newUserProfile);
        newUserProfile.setUser(newUser);
        Inbox newInbox = new Inbox(newUser);
        newUser.setInbox(newInbox);

        return save(newUser);
    }

    public UserProfile createNewProfile(User newUser, MultipartFile pictureFile) throws IOException {

        UserProfile newProfile = new UserProfile();
        newProfile.setUser(newUser);

        return profileService.modifyProfilePictureOf(newUser, newProfile.getId(), pictureFile);
    }

    @Transactional
    public User modifyTimeZone(User modifyingUser, String newTimeZone)
    {
        ZoneId newUserZone = ZoneId.of(newTimeZone);
        modifyingUser.setTimeZone(newUserZone);
        return save(modifyingUser);
    }

    @Transactional
    public User save(User user){
        return repository.save(user);
    }

    @Transactional
    public void unlockUser(String emailOrPhone)
    {
        User userToUnlock = findByEmailOrPhone(emailOrPhone).orElse(null);
        assert userToUnlock != null;

        userToUnlock.setLastLoginTime(ZonedDateTime.now(ZoneId.of("UTC")));
        save(userToUnlock);
    }

    @Transactional
    public User modifyForumUsername(User modifyingUser, String newUsername){
        modifyingUser.setForumUsername(newUsername);
        return save(modifyingUser);

    }

    private HomePageData getPaginatedResumes(int pageNumber, int pageSize, User user)
    {
        PageRequest request =
                PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "creationDate"));

        UserProfile profile = user.getProfile();
        Page<Resume> foundPage = resumeRepository.findAll(request);

        return new HomePageData(foundPage.getContent(), foundPage.getNumber(), foundPage.getTotalElements(),
                foundPage.getTotalPages(), foundPage.getSize(), profile,null, profile.getResumeSearchConfigurations());
    }

    public HomePageData getHomePageData(User user, int pageNumber, int pageSize,
                                        String searchToken){
        if (searchToken != null)
        {
            SearchResultDTO searchResult = elasticSearchService.getFromCache(searchToken);
            if (searchResult != null)
            {
                UserProfile profile = user.getProfile();
                List<UUID> matchedResults = searchResult.results();

                int numPages = Math.max(1, Math.ceilDiv(matchedResults.size(), pageSize));

                List<UUID> matchedList = elasticSearchService.getNecessaryResults(matchedResults, pageNumber, pageSize);

                List<Resume> foundResumes = resumeRepository.findAllById(matchedList);

                return new HomePageData(foundResumes, pageNumber,
                        matchedResults.size(), numPages, pageSize, profile, searchResult.searchToken(),
                        profile.getResumeSearchConfigurations());
            }
            //Otherwise, we simply return the whole forum post information, paginated.
            else{
                return getPaginatedResumes(pageNumber, pageSize, user);
            }
        }
        return getPaginatedResumes(pageNumber, pageSize, user);
    }

    public HomePageData getSearchResult(ResumeSpecsForm forumSpecsForm,
                                                User user, int pageNumber, int pageSize){
        UserProfile profile = user.getProfile();
        SearchResultDTO resultDTO = elasticSearchService.executeSearch(forumSpecsForm);

        List<UUID> matchedResults = resultDTO.results();

        int numPages = Math.max(1, Math.ceilDiv(matchedResults.size(), pageSize));

        List<UUID> matchedList = elasticSearchService.getNecessaryResults(matchedResults, pageNumber, pageSize);

        List<Resume> matchedResumes = resumeRepository.findAllById(matchedList);
        return new HomePageData(matchedResumes, pageNumber, matchedResults.size(),
                numPages, pageSize, profile, resultDTO.searchToken(), profile.getResumeSearchConfigurations());
    }

    public Bucket returnUserBucket(String loginEmail){
        //The lambda is to get around the fact that building
        // with supplying a bucket configuration directly is deprecated, thank god for lambdas
        return proxyManager.builder().build(loginEmail, () -> bucketConfiguration);
    }


    //Friendship is symmetric, so it doesn't matter for this method who the users are
    public void addFriend(User sender, User recipient)
    {
        FriendRelationship friendRelationship = new FriendRelationship(sender, recipient);

        friendRelationshipRepository.save(friendRelationship);
    }

}

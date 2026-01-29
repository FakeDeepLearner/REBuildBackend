package com.rebuild.backend.service.user_services;


import com.rebuild.backend.model.entities.profile_entities.ProfileSettings;
import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.resume_entities.search_entities.ResumeSearchConfiguration;
import com.rebuild.backend.model.entities.users.CaptchaVerificationRecord;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.forms.auth_forms.LoginForm;
import com.rebuild.backend.model.forms.auth_forms.SignupForm;
import com.rebuild.backend.model.forms.dtos.CredentialValidationDTO;
import com.rebuild.backend.model.forms.dtos.forum_dtos.SearchResultDTO;
import com.rebuild.backend.model.forms.resume_forms.ResumeSpecsForm;
import com.rebuild.backend.model.responses.HomePageData;
import com.rebuild.backend.model.responses.PasswordFeedbackResponse;
import com.rebuild.backend.model.responses.UserProfileResponse;
import com.rebuild.backend.repository.resume_repositories.ResumeRepository;
import com.rebuild.backend.repository.user_repositories.CaptchaVerificationRepository;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import com.rebuild.backend.service.util_services.CustomPasswordService;
import com.rebuild.backend.service.util_services.ElasticSearchService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class UserService{

    private final int SALT_BYTE_LENGTH = 16;

    private final UserRepository repository;

    private final PasswordEncoder encoder;

    private final ResumeRepository resumeRepository;

    private final ProfileService profileService;

    private final Dotenv dotenv;

    private final ElasticSearchService elasticSearchService;


    @Autowired
    public UserService(UserRepository repository,
                       ResumeRepository resumeRepository,
                       ProfileService profileService,
                       Dotenv dotenv, ElasticSearchService elasticSearchService) {
        this.repository = repository;
        this.profileService = profileService;
        this.dotenv = dotenv;
        this.elasticSearchService = elasticSearchService;
        this.encoder = new BCryptPasswordEncoder();
        this.resumeRepository = resumeRepository;
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

    @Transactional
    public void removePhoneOf(User deletingUser){
        deletingUser.setPhoneNumber(null);
        repository.save(deletingUser);
    }



    private String generateSaltValue(){
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_BYTE_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    @Transactional
    public User createNewUser(SignupForm signupForm, MultipartFile pictureFile) throws IOException {
        String generatedBackupForumName = "Anonymous#" + UUID.randomUUID().toString().substring(0, 8);
        String generatedSalt = generateSaltValue();
        String pepper = dotenv.get("PEPPER_VALUE");
        String encodedPassword = encoder.encode(signupForm.password() + generatedSalt + pepper);
        User newUser = new User(encodedPassword, signupForm.email(),
                signupForm.phoneNumber(), generatedSalt);

        if (signupForm.forumUsername() != null) {
            newUser.setForumUsername(signupForm.forumUsername());
        }

        newUser.setBackupForumUsername(generatedBackupForumName);

        UserProfile newUserProfile = createNewProfile(newUser, pictureFile);
        newUser.setUserProfile(newUserProfile);
        newUserProfile.setUser(newUser);

        return repository.save(newUser);
    }

    private UserProfile createNewProfile(User newUser, MultipartFile pictureFile) throws IOException {

        UserProfile newProfile = new UserProfile();
        ProfileSettings settings = new ProfileSettings(false,
                false, false);
        settings.setAssociatedProfile(newProfile);
        newProfile.setSettings(settings);

        newUser.setUserProfile(newProfile);
        newProfile.setUser(newUser);

        return profileService.modifyProfilePictureOf(newUser, pictureFile);
    }

    public UserProfileResponse loadProfileFromForum(UUID clickedUserId)
    {
        return null;
    }

    @Transactional
    public User modifyForumUsername(User modifyingUser, String newUsername){
        modifyingUser.setForumUsername(newUsername);
        return repository.save(modifyingUser);

    }

    private HomePageData getPaginatedResumes(int pageNumber, int pageSize, User user)
    {
        PageRequest request =
                PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "creationDate"));

        Page<Resume> foundPage = resumeRepository.findAll(request);

        return new HomePageData(foundPage.getContent(), foundPage.getNumber(), foundPage.getTotalElements(),
                foundPage.getTotalPages(), foundPage.getSize(),null);
    }

    public HomePageData getHomePageData(User user, int pageNumber, int pageSize,
                                        String searchToken){
        if (searchToken != null)
        {
            SearchResultDTO searchResult = elasticSearchService.getFromCache(searchToken);
            if (searchResult != null)
            {
                List<UUID> matchedResults = searchResult.results();

                int numPages = Math.max(1, Math.ceilDiv(matchedResults.size(), pageSize));

                List<UUID> matchedList = elasticSearchService.getNecessaryResults(matchedResults, pageNumber, pageSize);

                List<Resume> foundResumes = resumeRepository.findAllById(matchedList);

                return new HomePageData(foundResumes, pageNumber,
                        matchedResults.size(), numPages, pageSize, searchResult.searchToken());
            }
            //Otherwise, we simply return the whole forum post information, paginated.
            else{
                return getPaginatedResumes(pageNumber, pageSize, user);
            }
        }
        return getPaginatedResumes(pageNumber, pageSize, user);
    }

    public HomePageData getSearchResult(ResumeSearchConfiguration searchConfiguration, User user,
                                        int pageNumber, int pageSize)
    {
        SearchResultDTO resultDTO = elasticSearchService.executeSearch(searchConfiguration);

        List<UUID> matchedResults = resultDTO.results();

        int numPages = Math.max(1, Math.ceilDiv(matchedResults.size(), pageSize));

        List<UUID> matchedList = elasticSearchService.getNecessaryResults(matchedResults, pageNumber, pageSize);

        List<Resume> matchedResumes = resumeRepository.findAllById(matchedList);
        return new HomePageData(matchedResumes, pageNumber, matchedResults.size(),
                numPages, pageSize, resultDTO.searchToken());
    }

    public HomePageData getSearchResult(ResumeSpecsForm forumSpecsForm,
                                                User user, int pageNumber, int pageSize){
        SearchResultDTO resultDTO = elasticSearchService.executeSearch(forumSpecsForm);

        List<UUID> matchedResults = resultDTO.results();

        int numPages = Math.max(1, Math.ceilDiv(matchedResults.size(), pageSize));

        List<UUID> matchedList = elasticSearchService.getNecessaryResults(matchedResults, pageNumber, pageSize);

        List<Resume> matchedResumes = resumeRepository.findAllById(matchedList);
        return new HomePageData(matchedResumes, pageNumber, matchedResults.size(),
                numPages, pageSize, resultDTO.searchToken());
    }

}

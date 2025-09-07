package com.rebuild.backend.service.user_services;


import com.rebuild.backend.model.entities.messaging_and_friendship_entities.FriendRelationship;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.forms.auth_forms.LoginForm;
import com.rebuild.backend.model.forms.auth_forms.SignupForm;
import com.rebuild.backend.model.forms.dtos.CredentialValidationDTO;
import com.rebuild.backend.model.responses.HomePageData;
import com.rebuild.backend.repository.FriendRelationshipRepository;
import com.rebuild.backend.repository.ResumeRepository;
import com.rebuild.backend.repository.UserRepository;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class UserService{


    private final UserRepository repository;

    private final PasswordEncoder encoder;

    private final SessionRegistry sessionRegistry;

    private final ResumeRepository resumeRepository;

    private final ProxyManager<String> proxyManager;
    private final BucketConfiguration bucketConfiguration;

    private final FriendRelationshipRepository friendRelationshipRepository;


    @Autowired
    public UserService(UserRepository repository,
                       SessionRegistry sessionRegistry,
                       ResumeRepository resumeRepository,
                       ProxyManager<String> proxyManager,
                       BucketConfiguration bucketConfiguration,
                      FriendRelationshipRepository friendRelationshipRepository) {
        this.repository = repository;
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


    public Optional<User> findByEmail(String email){
        return repository.findByEmail(email);
    }


    public User findByEmailNoOptional(String email){
        return findByEmail(email).orElse(null);
    }


    @Transactional
    public void changePassword(User changingUser, String newRawPassword){
        String userSalt = changingUser.getSaltValue();
        String pepper = System.getenv("PEPPER_VALUE");
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

    public CredentialValidationDTO validateLoginCredentials(LoginForm form) {
        String formField = form.emailOrPhone();

        User foundUser;
        String userChannel;
        if(formField.contains("@"))
        {
            //If we can't find a user with the provided email address, we simply return false
            foundUser = repository.findByEmail(formField).
                    orElse(null);
            userChannel = "email";
        }

        else
        {
            //Same logic as above
            foundUser = repository.findByPhoneNumber(formField).orElse(null);
            userChannel = "sms";
        }

        if (foundUser == null) {
            return new CredentialValidationDTO(false, "whatever", "whatever");
        }

        String userSalt = foundUser.getSaltValue();
        String pepper = System.getenv("PEPPER_VALUE");

        return new CredentialValidationDTO(encoder.matches(form.password() + userSalt + pepper,
                foundUser.getPassword()), foundUser.getEmail(), userChannel);

    }

    @Transactional
    public void removePhoneOf(User deletingUser){
        deletingUser.setPhoneNumber(null);
        save(deletingUser);
    }

    private String generateSaltValue(int length){
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[length];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    @Transactional
    public User createNewUser(SignupForm signupForm){
        String generatedSalt = generateSaltValue(16);
        String pepper = System.getenv("PEPPER_VALUE");
        String encodedPassword = encoder.encode(signupForm.password() + generatedSalt + pepper);
        ZoneId userTimeZone = ZoneId.of(signupForm.timezoneAsString());
        User newUser = new User(encodedPassword, signupForm.email(),
                signupForm.phoneNumber(), signupForm.forumUsername(), generatedSalt, userTimeZone);
        return save(newUser);
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
    protected void blockInactiveUsers(LocalDateTime cutoff){
        List<User> inactiveUsers = repository.findByLastLoginTimeBefore(cutoff);
        inactiveUsers.forEach((user) -> {
            user.setAccountNonExpired(false);
            save(user);
        });
    }

    //The following method will run every day at midnight (local (EST) time)
    @Scheduled(cron = "@midnight")
    @Transactional
    public void blockingScheduledTask(){
        //Block all users that haven't logged in for (at least) 6 months
        LocalDateTime cutoff = LocalDateTime.now().minusMonths(6);
        blockInactiveUsers(cutoff);
    }


    @Transactional
    public User modifyForumUsername(User modifyingUser, String newUsername){
        modifyingUser.setForumUsername(newUsername);
        return save(modifyingUser);

    }

    public HomePageData loadHomePageInformation(User user, int pageNumber, int pageSize){
        Pageable pageableResult = PageRequest.of(pageNumber, pageSize,
                Sort.by("creationDate").descending().
                        and(Sort.by("lastModifiedDate").descending()));
        Page<Resume> resultingPage = resumeRepository.findAllById(user.getId(), pageableResult);
        return new HomePageData(resultingPage.getContent(), resultingPage.getNumber(), resultingPage.getTotalElements(),
                resultingPage.getTotalPages(), pageSize, user.getProfile());
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

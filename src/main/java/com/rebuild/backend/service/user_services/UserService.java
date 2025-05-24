package com.rebuild.backend.service.user_services;

import com.rebuild.backend.exceptions.conflict_exceptions.EmailAlreadyExistsException;
import com.rebuild.backend.exceptions.not_found_exceptions.UserNotFoundException;
import com.rebuild.backend.exceptions.not_found_exceptions.WrongPasswordException;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.forms.auth_forms.LoginForm;
import com.rebuild.backend.model.forms.auth_forms.SignupForm;
import com.rebuild.backend.model.responses.HomePageData;
import com.rebuild.backend.repository.ResumeRepository;
import com.rebuild.backend.utils.OptionalValueAndErrorResult;
import com.rebuild.backend.repository.UserRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.CONFLICT;

@Service
@Transactional(readOnly = true)
public class UserService{


    private final UserRepository repository;

    private final PasswordEncoder encoder;

    private final SessionRegistry sessionRegistry;

    private final ResumeRepository resumeRepository;


    @Autowired
    public UserService(UserRepository repository,
                       @Qualifier("peppered") PasswordEncoder encoder,
                       SessionRegistry sessionRegistry,
                       ResumeRepository resumeRepository){
        this.repository = repository;
        this.encoder = encoder;
        this.sessionRegistry = sessionRegistry;
        this.resumeRepository = resumeRepository;
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
    public void changePassword(UUID userID, String newRawPassword){
        String newHashedPassword = encoder.encode(newRawPassword);
        repository.changePassword(userID, newHashedPassword);
    }

    @Transactional
    public void changeEmail(UUID userID, String newEmail){
        try {
            repository.changeEmail(userID, newEmail);
        }
        catch (DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            if (cause instanceof ConstraintViolationException violationException){
                if (Objects.equals(violationException.getConstraintName(), "uk_email")){
                    throw new EmailAlreadyExistsException("This email address already exists");
                }
            }
        }
    }


    public void validateLoginCredentials(LoginForm form) {
        String formField = form.email();
        User foundUser = repository.findByEmail(formField).
                orElseThrow(() -> new UserNotFoundException("A user with the specified email does not exist"));;

        String hashedPassword = encoder.encode(form.password());

        if(!foundUser.getPassword().equals(hashedPassword)){
            throw new WrongPasswordException("Wrong password");
        }

        foundUser.setLastLoginTime(LocalDateTime.now());
        save(foundUser);

    }

    @Transactional
    public void removePhoneOf(User deletingUser){
        deletingUser.setPhoneNumber(null);
        save(deletingUser);
    }

    @Transactional
    public OptionalValueAndErrorResult<User> createNewUser(SignupForm signupForm){
        String encodedPassword = encoder.encode(signupForm.password());
        User newUser = new User(encodedPassword, signupForm.email(),
                signupForm.phoneNumber(), signupForm.forumUsername());
        try {
            User savedUser = save(newUser);
            return  OptionalValueAndErrorResult.of(savedUser, CREATED);
        }
        catch (DataIntegrityViolationException integrityViolationException){
            Throwable cause = integrityViolationException.getCause();
            if (cause instanceof ConstraintViolationException violationException){
                String violatedConstraint = violationException.getConstraintName();
                switch (violatedConstraint){
                    case "uk_email" -> {
                        return  OptionalValueAndErrorResult.of("This email is taken", CONFLICT);

                    }
                    case "uk_phone_number" -> {
                        return OptionalValueAndErrorResult.of("This phone is already associated with another account",
                                CONFLICT);

                    }
                    case "uk_forum_username" -> {
                        return OptionalValueAndErrorResult.of("This forum username is already associated with another account",
                                CONFLICT);
                    }

                    //This should never happen
                    case null -> {}

                    default -> throw new IllegalStateException("Unexpected value: " + violatedConstraint);
                }
            }
        }
        //Unknown error, signal http 500
        return OptionalValueAndErrorResult.empty();
    }

    @Transactional
    public User save(User user){
        return repository.save(user);
    }

    @Transactional
    public void lockUserAccount(String email){
        User user = repository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(""));
        user.setAccountNonLocked(false);
        save(user);
    }

    @Transactional
    public void unlockUserAccount(String email){
        User user = repository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(""));
        user.setAccountNonLocked(true);
        save(user);
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
    public OptionalValueAndErrorResult<User> modifyForumUsername(User modifyingUser, String newUsername){
        String oldUsername = modifyingUser.getForumUsername();
        try {
            modifyingUser.setForumUsername(newUsername);
            User savedUser = save(modifyingUser);
            return OptionalValueAndErrorResult.of(savedUser, OK);
        }
        catch (DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            modifyingUser.setForumUsername(oldUsername);
            if (cause instanceof ConstraintViolationException violationException){
                if (Objects.equals(violationException.getConstraintName(), "uk_forum_username")){
                    return OptionalValueAndErrorResult.of(modifyingUser, "This username is taken", CONFLICT);
                }
            }
            //Unknown error, signal 500
            return OptionalValueAndErrorResult.of(modifyingUser, INTERNAL_SERVER_ERROR);
        }
    }

    public HomePageData loadHomePageInformation(User user, int pageNumber, int pageSize){
        Pageable pageableResult = PageRequest.of(pageNumber, pageSize,
                Sort.by("creationDate").descending().
                        and(Sort.by("lastModifiedDate").descending()));
        Page<Resume> resultingPage = resumeRepository.findAllById(user.getId(), pageableResult);
        return new HomePageData(resultingPage.getContent(), resultingPage.getNumber(), resultingPage.getTotalElements(),
                resultingPage.getTotalPages(), pageSize, user.getProfile());
    }

}

package com.rebuild.backend.service.user_services;

import com.rebuild.backend.exceptions.conflict_exceptions.EmailAlreadyExistsException;
import com.rebuild.backend.exceptions.not_found_exceptions.UserNotFoundException;
import com.rebuild.backend.exceptions.not_found_exceptions.WrongPasswordException;
import com.rebuild.backend.model.entities.resume_entities.PhoneNumber;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.forms.auth_forms.LoginForm;
import com.rebuild.backend.utils.OptionalValueAndErrorResult;
import com.rebuild.backend.repository.UserRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
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
@Transactional
public class UserService{


    private final UserRepository repository;

    private final PasswordEncoder encoder;

    private final SessionRegistry sessionRegistry;


    @Autowired
    public UserService(UserRepository repository,
                       @Qualifier("peppered") PasswordEncoder encoder,
                       SessionRegistry sessionRegistry){
        this.repository = repository;
        this.encoder = encoder;
        this.sessionRegistry = sessionRegistry;
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


    public void changePassword(UUID userID, String newRawPassword){
        String newHashedPassword = encoder.encode(newRawPassword);
        repository.changePassword(userID, newHashedPassword);
    }

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

    public void removePhoneOf(User deletingUser){
        deletingUser.setPhoneNumber(null);
        save(deletingUser);
    }

    public OptionalValueAndErrorResult<User> createNewUser(String rawPassword, String email, PhoneNumber phoneNumber){
        String encodedPassword = encoder.encode(rawPassword);
        User newUser = new User(encodedPassword, email, phoneNumber);
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
                        return OptionalValueAndErrorResult.of("This phone is already associated with another account", CONFLICT);

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

    public User save(User user){
        return repository.save(user);
    }

    public void lockUserAccount(String email){
        User user = repository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(""));
        user.setAccountNonLocked(false);
        save(user);
    }

    public void unlockUserAccount(String email){
        User user = repository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(""));
        user.setAccountNonLocked(true);
        save(user);
    }
    public void blockInactiveUsers(LocalDateTime cutoff){
        List<User> inactiveUsers = repository.findByLastLoginTimeBefore(cutoff);
        inactiveUsers.forEach((user) -> {
            user.setAccountNonExpired(false);
            repository.save(user);
        });
    }

    public void reactivateUserCredentials(User user){
        user.setAccountNonExpired(true);
        save(user);
    }

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

    public User modifyForumPassword(User modifyingUser, String newRawPassword){
        String encodedPassword = encoder.encode(newRawPassword);
        modifyingUser.setPassword(encodedPassword);
        return save(modifyingUser);
    }

    //The following method will run every day at midnight (local (EST) time)
    @Scheduled(cron = "@midnight")
    public void blockingScheduledTask(){
        //Block all users that haven't logged in for (at least) 6 months
        LocalDateTime cutoff = LocalDateTime.now().minusMonths(6);
        blockInactiveUsers(cutoff);
    }

}

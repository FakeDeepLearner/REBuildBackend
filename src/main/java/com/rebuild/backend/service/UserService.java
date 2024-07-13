package com.rebuild.backend.service;

import com.rebuild.backend.exceptions.conflict_exceptions.EmailAlreadyExistsException;
import com.rebuild.backend.exceptions.not_found_exceptions.UserNotFoundException;
import com.rebuild.backend.exceptions.conflict_exceptions.UsernameAlreadyExistsException;
import com.rebuild.backend.exceptions.not_found_exceptions.WrongPasswordException;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.User;
import com.rebuild.backend.model.forms.LoginForm;
import com.rebuild.backend.repository.UserRepository;
import com.rebuild.backend.utils.EmailOrUsernameDecider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService{


    private final UserRepository repository;

    private final PasswordEncoder encoder;

    private final SessionRegistry sessionRegistry;

    private final EmailOrUsernameDecider decider;

    @Autowired
    public UserService(UserRepository repository,
                       @Qualifier("peppered") PasswordEncoder encoder,
                       SessionRegistry sessionRegistry, EmailOrUsernameDecider decider){
        this.repository = repository;
        this.encoder = encoder;
        this.sessionRegistry = sessionRegistry;
        this.decider = decider;
    }

    public void invalidateAllSessions(String username){
        List<SessionInformation> allSessions = sessionRegistry.getAllSessions(username, false);
        if (allSessions != null) {
            allSessions.forEach(SessionInformation::expireNow);
        }
    }

    public Optional<User> findByID(UUID userID){
        return repository.findById(userID);
    }


    public Optional<User> findByEmail(String email){
        return repository.findByEmail(email);
    }

    public Optional<User> findByUsername(String username) {return repository.findByUsername(username);}

    public void changePassword(UUID userID, String newPassword){
        String newHashedPassword = encoder.encode(newPassword);
        repository.changePassword(userID, newHashedPassword);
    }

    public void changeEmail(UUID userID, String newEmail){
        repository.changeEmail(userID, newEmail);
    }

    public List<Resume> getAllResumesById(UUID userID){
        return repository.getAllResumesByID(userID);
    }

    public void validateLoginCredentials(LoginForm form) {
        String formField = form.emailOrUsername();
        User foundUser;
        if(decider.isInputEmail(formField)){
            foundUser = repository.findByEmail(formField).
                    orElseThrow(() -> new UserNotFoundException("A user with the specified email does not exist"));
        }
        else{
            foundUser = repository.findByUsername(formField).
                    orElseThrow(() -> new UserNotFoundException("A user with the specified username does not exist"));
        }

        String hashedPassword = encoder.encode(form.password());

        if(!foundUser.getPassword().equals(hashedPassword)){
            throw new WrongPasswordException("Wrong password");
        }

    }

    private boolean checkUsernameExists(String username){
        Optional<User> checkedUser = repository.findByUsername(username);
        return checkedUser.isPresent();
    }

    private boolean checkEmailExists(String email){
        Optional<User> checkedUser = repository.findByEmail(email);
        return checkedUser.isPresent();
    }

    public User createNewUser(String username, String rawPassword, String email){
        if (checkUsernameExists(username)){
            throw new UsernameAlreadyExistsException("This username is taken.");
        }

        if (checkEmailExists(email)){
            throw new EmailAlreadyExistsException("There is already an account with this email address.");
        }
        String encodedPassword = encoder.encode(rawPassword);
        User newUser = new User(username, encodedPassword, email);
        return save(newUser);
    }

    public User save(User user){
        return repository.save(user);
    }


}

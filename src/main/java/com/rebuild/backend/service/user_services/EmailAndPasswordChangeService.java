package com.rebuild.backend.service.user_services;

import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import io.github.cdimascio.dotenv.Dotenv;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class EmailAndPasswordChangeService {

    private final Dotenv dotenv;


    private final PasswordEncoder encoder;

    private final UserRepository userRepository;

    public EmailAndPasswordChangeService(Dotenv dotenv, PasswordEncoder encoder,
                                         UserRepository userRepository) {
        this.dotenv = dotenv;
        this.encoder = encoder;
        this.userRepository = userRepository;
    }

    @Transactional
    public void changePassword(User changingUser, String newRawPassword){
        String userSalt = changingUser.getSaltValue();
        String pepper = dotenv.get("PEPPER_VALUE");
        String newHashedPassword = encoder.encode(newRawPassword + userSalt + pepper);

        changingUser.setPassword(newHashedPassword);
        userRepository.save(changingUser);
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
}

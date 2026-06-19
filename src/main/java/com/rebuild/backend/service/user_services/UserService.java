package com.rebuild.backend.service.user_services;


import com.rebuild.backend.model.entities.user_entities.UserProfile;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.*;

@Service
public class UserService{

    private final UserRepository repository;

    private final PasswordEncoder encoder;


    @Autowired
    public UserService(UserRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    private String generateSaltValue(){
        SecureRandom random = new SecureRandom();
        //16 bytes = 128-bit salt, which is what is recommended
        int saltByteLength = 16;
        byte[] salt = new byte[saltByteLength];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /*
    @Transactional
    public User createNewUser(SignupFinalizationForm finalizationForm, String mfaSecret){

        String generatedSalt = generateSaltValue();
        String pepper = System.getenv("PEPPER_VALUE");
        String encodedPassword = encoder.encode(finalizationForm.password() + generatedSalt + pepper);
        User newUser = new User(encodedPassword, finalizationForm.email(),
                 generatedSalt, mfaSecret);

        newUser.setForumUsername(finalizationForm.forumUsername());

        newUser.setAnonymizedNameBase(UUID.randomUUID().toString().substring(0, 8));

        UserProfile newUserProfile = createNewProfile(newUser);
        newUser.setUserProfile(newUserProfile);
        newUserProfile.setUser(newUser);

        return newUser;
    }
    */

    private UserProfile createNewProfile(User newUser){

        UserProfile newProfile = new UserProfile();
        newUser.setUserProfile(newProfile);
        newProfile.setUser(newUser);

        return newProfile;

    }

    @Transactional
    public User modifyForumUsername(User modifyingUser, String newUsername){
        modifyingUser.setForumUsername(newUsername);
        return repository.save(modifyingUser);

    }
}

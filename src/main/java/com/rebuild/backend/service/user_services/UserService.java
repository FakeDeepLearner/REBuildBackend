package com.rebuild.backend.service.user_services;


import com.rebuild.backend.model.entities.user_entities.UserProfile;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.exceptions.UserAuthException;
import com.rebuild.backend.model.forms.auth_forms.SignupFinalizationForm;
import com.rebuild.backend.model.forms.auth_forms.SignupInitializationForm;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import com.rebuild.backend.service.util_services.CloudinaryService;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.*;

@Service
public class UserService{

    private final UserRepository repository;

    private final PasswordEncoder encoder;

    private final Dotenv dotenv;

    private final CloudinaryService cloudinaryService;


    @Autowired
    public UserService(UserRepository repository, PasswordEncoder encoder,
                       Dotenv dotenv, CloudinaryService cloudinaryService) {
        this.repository = repository;
        this.dotenv = dotenv;
        this.cloudinaryService = cloudinaryService;
        this.encoder = encoder;
    }

    @Transactional
    public void removePhoneOf(User deletingUser){
        deletingUser.setPhoneNumber(null);
        repository.save(deletingUser);
    }

    private String generateSaltValue(){
        SecureRandom random = new SecureRandom();
        //16 bytes = 128-bit salt, which is what is recommended
        int saltByteLength = 16;
        byte[] salt = new byte[saltByteLength];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    @Transactional
    public User createNewUser(SignupFinalizationForm finalizationForm, String mfaSecret){

        String generatedSalt = generateSaltValue();
        String pepper = dotenv.get("PEPPER_VALUE");
        String encodedPassword = encoder.encode(finalizationForm.password() + generatedSalt + pepper);
        User newUser = new User(encodedPassword, finalizationForm.email(),
                finalizationForm.phoneNumber(), generatedSalt, mfaSecret);

        newUser.setForumUsername(finalizationForm.forumUsername());

        newUser.setAnonymizedNameBase(UUID.randomUUID().toString().substring(0, 8));

        UserProfile newUserProfile = createNewProfile(newUser);
        newUser.setUserProfile(newUserProfile);
        newUserProfile.setUser(newUser);

        return newUser;
    }

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

package com.rebuild.backend.service.user_services;


import com.rebuild.backend.model.entities.profile_entities.ProfileSettings;
import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.forms.auth_forms.SignupForm;
import com.rebuild.backend.model.responses.UserProfileResponse;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import com.rebuild.backend.service.util_services.CloudinaryService;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;

@Service
@Transactional(readOnly = true)
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
        //16 bytes = 128 bit salt, which is what is recommended
        int saltByteLength = 16;
        byte[] salt = new byte[saltByteLength];
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

        return cloudinaryService.modifyProfilePictureOf(newUser, pictureFile);
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
}

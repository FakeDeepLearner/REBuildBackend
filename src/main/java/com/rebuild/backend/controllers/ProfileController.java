package com.rebuild.backend.controllers;

import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.forms.profile_forms.FullProfileForm;
import com.rebuild.backend.model.forms.resume_forms.*;
import com.rebuild.backend.model.responses.UserProfileResponse;
import com.rebuild.backend.service.user_services.ProfileService;
import com.rebuild.backend.service.user_services.UserService;
import com.rebuild.backend.service.util_services.CloudinaryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    private final UserService userService;

    private final CloudinaryService cloudinaryService;

    @Autowired
    public ProfileController(ProfileService profileService, UserService userService,
                             CloudinaryService cloudinaryService) {
        this.profileService = profileService;
        this.userService = userService;
        this.cloudinaryService = cloudinaryService;
    }


    @GetMapping("/load_profile")
    public UserProfileResponse loadOwnProfile(@AuthenticationPrincipal User user)
    {
        return profileService.loadUserProfile(user, user.getId());
    }

    @GetMapping("/load_profile/{clicked_user_id}")
    public UserProfileResponse loadClickedUserProfile(@AuthenticationPrincipal User user, @PathVariable UUID clicked_user_id)
    {
        return profileService.loadUserProfile(user, clicked_user_id);
    }

    @PutMapping(value = "/update_image", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public UserProfileResponse changeImage(@AuthenticationPrincipal User changingUser,
                                   @RequestPart(name = "file") MultipartFile pictureFile)
    {
        return cloudinaryService.changeProfilePicture(changingUser, pictureFile);
    }

    @DeleteMapping("/remove_image")
    @ResponseStatus(HttpStatus.OK)
    public UserProfileResponse removeProfileImage(@AuthenticationPrincipal User user){
        return cloudinaryService.removeProfilePicture(user);
    }

    @DeleteMapping("/delete_phone")
    @ResponseStatus(NO_CONTENT)
    public void removePhoneNumber(@AuthenticationPrincipal User authenticatedUser) {
        userService.removePhoneOf(authenticatedUser);
    }
}

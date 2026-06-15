package com.rebuild.backend.controllers;

import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.forms.profile_forms.ProfilePrivacySettingsForm;
import com.rebuild.backend.model.responses.user_responses.UserProfileResponse;
import com.rebuild.backend.service.user_services.ProfileService;
import com.rebuild.backend.service.util_services.AWSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    private final AWSService awsService;

    @Autowired
    public ProfileController(ProfileService profileService, AWSService awsService) {
        this.profileService = profileService;
        this.awsService = awsService;
    }


    @GetMapping("/load_profile")
    public UserProfileResponse loadOwnProfile(@AuthenticationPrincipal User user)
    {
        return profileService.loadSelfProfile(user);
    }

    @GetMapping("/load_profile/{clicked_user_id}")
    public UserProfileResponse loadClickedUserProfile(@AuthenticationPrincipal User user, @PathVariable UUID clicked_user_id)
    {
        return profileService.loadUserProfile(user, clicked_user_id);
    }

    @PatchMapping(value = "/update_image", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public String changeImage(@AuthenticationPrincipal User changingUser,
                                   @RequestPart(name = "file") MultipartFile pictureFile)
    {
        return null;
    }

    @DeleteMapping("/remove_image")
    @ResponseStatus(HttpStatus.OK)
    public UserProfileResponse removeProfileImage(@AuthenticationPrincipal User user){
        return awsService.removeProfilePicture(user);
    }


    @PatchMapping("/update_privacy_settings")
    @ResponseStatus(HttpStatus.OK)
    public UserProfileResponse updateProfilePrivacy(@AuthenticationPrincipal User user,
                                                    @RequestBody ProfilePrivacySettingsForm privacySettingsForm)
    {
        return profileService.changeProfilePrivacySettings(user, privacySettingsForm);
    }
}

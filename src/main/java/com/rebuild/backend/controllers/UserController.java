package com.rebuild.backend.controllers;

import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.forms.profile_forms.ProfilePrivacySettingsForm;
import com.rebuild.backend.model.responses.user_responses.UserProfileResponse;
import com.rebuild.backend.service.user_services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
public class UserController {

    private final UserService userService;


    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/load_profile")
    public UserProfileResponse loadOwnProfile(@AuthenticationPrincipal User user)
    {
        return userService.loadSelfProfile(user);
    }

    @GetMapping("/load_profile/{clicked_user_id}")
    public UserProfileResponse loadClickedUserProfile(@AuthenticationPrincipal User user, @PathVariable UUID clicked_user_id)
    {
        return userService.loadUserProfile(user, clicked_user_id);
    }

    @PatchMapping("/update_privacy_settings")
    @ResponseStatus(HttpStatus.OK)
    public UserProfileResponse updateProfilePrivacy(@AuthenticationPrincipal User user,
                                                    @RequestBody ProfilePrivacySettingsForm privacySettingsForm)
    {
        return userService.changeProfilePrivacySettings(user, privacySettingsForm);
    }

    @PatchMapping("/update_location")
    @ResponseStatus(HttpStatus.OK)
    public String updateUserLocation(@AuthenticationPrincipal User user,
                                     @RequestBody String newLocation)
    {
        return userService.updateUserLocation(user, newLocation);
    }


    @PatchMapping("/update_biography")
    @ResponseStatus(HttpStatus.OK)
    public String updateUserBiography(@AuthenticationPrincipal User user,
                                     @RequestBody String newBiography)
    {
        return userService.updateUserBiography(user, newBiography);
    }
}

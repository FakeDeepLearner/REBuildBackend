package com.rebuild.backend.controllers;

import com.rebuild.backend.exceptions.profile_exceptions.NoProfileException;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.forms.profile_forms.FullProfileForm;
import com.rebuild.backend.model.forms.profile_forms.ProfileEducationForm;
import com.rebuild.backend.model.forms.profile_forms.ProfileExperienceForm;
import com.rebuild.backend.model.forms.profile_forms.ProfileHeaderForm;
import com.rebuild.backend.service.user_services.ProfileService;
import com.rebuild.backend.service.user_services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile/control")
public class ProfileController {
    private final UserService userService;

    private final ProfileService profileService;

    @Autowired
    public ProfileController(UserService userService,
                             ProfileService profileService) {
        this.userService = userService;
        this.profileService = profileService;
    }

    @PostMapping("/create_profile")
    @ResponseStatus(HttpStatus.CREATED)
    public UserProfile createProfileFor(@Valid @RequestBody FullProfileForm fullProfileForm,
                                        @AuthenticationPrincipal UserDetails creatingUserDetails) {
        User creatingUser = userService.findByEmailNoOptional(creatingUserDetails.getUsername());
        return profileService.createProfileFor(fullProfileForm, creatingUser);
    }

    @PatchMapping("/patch/header")
    @ResponseStatus(HttpStatus.OK)
    public UserProfile updateProfileHeader(@Valid @RequestBody ProfileHeaderForm headerForm,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        User updatingUser = userService.findByEmailNoOptional(userDetails.getUsername());
        if(updatingUser.getProfile() == null){
            throw new NoProfileException("No profile found for your account");
        }
        UserProfile profile = updatingUser.getProfile();
        return profileService.updateProfileHeader(profile, headerForm);
    }

    @PatchMapping("/patch/education")
    @ResponseStatus(HttpStatus.OK)
    public UserProfile updateProfileHeader(@Valid @RequestBody ProfileEducationForm educationForm,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        User updatingUser = userService.findByEmailNoOptional(userDetails.getUsername());
        if(updatingUser.getProfile() == null){
            throw new NoProfileException("No profile found for your account");
        }
        UserProfile profile = updatingUser.getProfile();
        return profileService.updateProfileEducation(profile, educationForm);
    }

    @PatchMapping("/patch/experiences")
    @ResponseStatus(HttpStatus.OK)
    public UserProfile updateProfileHeader(@Valid @RequestBody List<ProfileExperienceForm> experienceFormList,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        User updatingUser = userService.findByEmailNoOptional(userDetails.getUsername());
        if(updatingUser.getProfile() == null){
            throw new NoProfileException("No profile found for your account");
        }
        UserProfile profile = updatingUser.getProfile();
        return profileService.updateProfileExperiences(profile, experienceFormList);
    }

    @DeleteMapping("/delete/{profile_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfile(@PathVariable UUID profile_id){
        profileService.deleteProfile(profile_id);
    }

    @DeleteMapping("/delete/header/{profile_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfileHeader(@PathVariable UUID profile_id){
        profileService.deleteProfileHeader(profile_id);
    }

    @DeleteMapping("/delete/education/{profile_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfileEducation(@PathVariable UUID profile_id){
        profileService.deleteProfileEducation(profile_id);
    }

    @DeleteMapping("/delete/experiences/{profile_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfileExperience(@PathVariable UUID profile_id){
        profileService.deleteProfileExperiences(profile_id);
    }

    @DeleteMapping("/delete/experiences/{profile_id}/{experience_id}")
    @ResponseStatus(HttpStatus.OK)
    public UserProfile deleteSpecificExperience(@PathVariable UUID profile_id,
                                                @PathVariable UUID experience_id){
        return profileService.deleteSpecificProfileExperience(profile_id, experience_id);
    }
}

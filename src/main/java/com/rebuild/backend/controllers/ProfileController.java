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
                                        @AuthenticationPrincipal User authenticatedUser) {
        return profileService.createProfileFor(fullProfileForm, authenticatedUser);
    }

    @PatchMapping("/patch/header")
    @ResponseStatus(HttpStatus.OK)
    public UserProfile updateProfileHeader(@Valid @RequestBody ProfileHeaderForm headerForm,
                                           @AuthenticationPrincipal User authenticatedUser) {
        if(authenticatedUser.getProfile() == null){
            throw new NoProfileException("No profile found for your account");
        }
        UserProfile profile = authenticatedUser.getProfile();
        return profileService.updateProfileHeader(profile, headerForm);
    }

    @PatchMapping("/patch/education")
    @ResponseStatus(HttpStatus.OK)
    public UserProfile updateProfileHeader(@Valid @RequestBody ProfileEducationForm educationForm,
                                           @AuthenticationPrincipal User authenticatedUser) {
        if(authenticatedUser.getProfile() == null){
            throw new NoProfileException("No profile found for your account");
        }
        UserProfile profile = authenticatedUser.getProfile();
        return profileService.updateProfileEducation(profile, educationForm);
    }

    @PatchMapping("/patch/experiences")
    @ResponseStatus(HttpStatus.OK)
    public UserProfile updateProfileHeader(@Valid @RequestBody List<ProfileExperienceForm> experienceFormList,
                                           @AuthenticationPrincipal User authenticatedUser) {
        if(authenticatedUser.getProfile() == null){
            throw new NoProfileException("No profile found for your account");
        }
        UserProfile profile = authenticatedUser.getProfile();
        return profileService.updateProfileExperiences(profile, experienceFormList);
    }

    @DeleteMapping("/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfile(@AuthenticationPrincipal User deletingUser){
        profileService.deleteProfile(deletingUser.getProfile().getId());
    }

    @DeleteMapping("/delete/header")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfileHeader(@AuthenticationPrincipal User deletingUser){
        profileService.deleteProfileHeader(deletingUser.getProfile().getId());
    }

    @DeleteMapping("/delete/education")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfileEducation(@AuthenticationPrincipal User deletingUser){
        profileService.deleteProfileEducation(deletingUser.getProfile().getId());
    }

    @DeleteMapping("/delete/experiences")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfileExperience(@AuthenticationPrincipal User deletingUser){
        profileService.deleteProfileExperiences(deletingUser.getProfile().getId());
    }

    @DeleteMapping("/delete/experiences/{experience_id}")
    @ResponseStatus(HttpStatus.OK)
    public UserProfile deleteSpecificExperience(@AuthenticationPrincipal User deletingUser,
                                                @PathVariable UUID experience_id){
        return profileService.deleteSpecificProfileExperience(deletingUser.getProfile().getId(),
                experience_id);
    }

    @PutMapping("/update_profile")
    public UserProfile changeEntireProfile(@AuthenticationPrincipal User updatingUser,
                                           @Valid @RequestBody FullProfileForm profileForm){
        return profileService.updateEntireProfile(updatingUser.getProfile(), profileForm);
    }
}

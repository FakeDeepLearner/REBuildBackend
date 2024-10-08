package com.rebuild.backend.controllers;

import com.rebuild.backend.exceptions.not_found_exceptions.UserNotFoundException;
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

    @PostMapping("/create/{user_id}")
    @ResponseStatus(HttpStatus.CREATED)
    public UserProfile createProfileFor(@PathVariable UUID user_id,
                                        @Valid @RequestBody FullProfileForm fullProfileForm) {
        User creatingUser = userService.findByID(user_id).orElseThrow(() ->
                new UserNotFoundException("User not found"));
        return profileService.createProfileFor(fullProfileForm, creatingUser);
    }

    @PatchMapping("/patch/header/{user_id}")
    @ResponseStatus(HttpStatus.OK)
    public UserProfile updateProfileHeader(@PathVariable UUID user_id,
                                           @Valid @RequestBody ProfileHeaderForm headerForm){
        User updatingUser = userService.findByID(user_id).orElseThrow(() ->
                new UserNotFoundException("User with id " + user_id + " not found"));
        if(updatingUser.getProfile() == null){
            throw new NoProfileException("No profile found for your account");
        }
        UserProfile profile = updatingUser.getProfile();
        return profileService.updateProfileHeader(profile, headerForm);
    }

    @PatchMapping("/patch/education/{user_id}")
    @ResponseStatus(HttpStatus.OK)
    public UserProfile updateProfileHeader(@PathVariable UUID user_id,
                                           @Valid @RequestBody ProfileEducationForm educationForm){
        User updatingUser = userService.findByID(user_id).orElseThrow(() ->
                new UserNotFoundException("User with id " + user_id + " not found"));
        if(updatingUser.getProfile() == null){
            throw new NoProfileException("No profile found for your account");
        }
        UserProfile profile = updatingUser.getProfile();
        return profileService.updateProfileEducation(profile, educationForm);
    }

    @PatchMapping("/patch/experiences/{user_id}")
    @ResponseStatus(HttpStatus.OK)
    public UserProfile updateProfileHeader(@PathVariable UUID user_id,
                                           @Valid @RequestBody List<ProfileExperienceForm> experienceFormList){
        User updatingUser = userService.findByID(user_id).orElseThrow(() ->
                new UserNotFoundException("User with id " + user_id + " not found"));
        if(updatingUser.getProfile() == null){
            throw new NoProfileException("No profile found for your account");
        }
        UserProfile profile = updatingUser.getProfile();
        return profileService.updateProfileExperiences(profile, experienceFormList);
    }

    @DeleteMapping("/delete/{user_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfile(@PathVariable UUID user_id){
        profileService.deleteProfile(user_id);
    }

    @DeleteMapping("/delete/header/{user_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfileHeader(@PathVariable UUID user_id){
        profileService.deleteProfileHeader(user_id);
    }

    @DeleteMapping("/delete/education/{user_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfileEducation(@PathVariable UUID user_id){
        profileService.deleteProfileEducation(user_id);
    }

    @DeleteMapping("/delete/experiences/{user_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfileExperience(@PathVariable UUID user_id){
        profileService.deleteProfileExperiences(user_id);
    }

    @DeleteMapping("/delete/experiences/{user_id}/{experience_id}")
    @ResponseStatus(HttpStatus.OK)
    public UserProfile deleteSpecificExperience(@PathVariable UUID user_id,
                                                @PathVariable UUID experience_id){
        return profileService.deleteSpecificProfileExperience(user_id, experience_id);
    }
}

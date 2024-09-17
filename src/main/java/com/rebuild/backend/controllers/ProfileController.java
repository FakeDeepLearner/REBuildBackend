package com.rebuild.backend.controllers;

import com.rebuild.backend.exceptions.not_found_exceptions.UserNotFoundException;
import com.rebuild.backend.exceptions.profile_exceptions.NoProfileException;
import com.rebuild.backend.model.entities.User;
import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.forms.profile_forms.FullProfileForm;
import com.rebuild.backend.model.forms.profile_forms.ProfileEducationForm;
import com.rebuild.backend.model.forms.profile_forms.ProfileExperienceForm;
import com.rebuild.backend.model.forms.profile_forms.ProfileHeaderForm;
import com.rebuild.backend.service.ProfileService;
import com.rebuild.backend.service.UserService;
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
        UserProfile newProfile = profileService.createProfile(fullProfileForm);
        newProfile.setUser(creatingUser);
        creatingUser.setProfile(newProfile);
        userService.save(creatingUser);
        return newProfile;
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
        User deletingUser = userService.findByID(user_id).orElseThrow(() ->
                new UserNotFoundException("User with id " + user_id + " not found"));
        if(deletingUser.getProfile() == null){
            throw new NoProfileException("No profile found to delete");
        }
        UserProfile profile = deletingUser.getProfile();
        profileService.deleteProfile(profile);
    }
}

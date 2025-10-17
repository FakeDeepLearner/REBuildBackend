package com.rebuild.backend.controllers;

import com.rebuild.backend.model.entities.resume_entities.Education;
import com.rebuild.backend.model.entities.resume_entities.Header;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.forms.resume_forms.*;
import com.rebuild.backend.service.user_services.ProfileService;
import com.rebuild.backend.service.user_services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    @PostMapping(value = "/update_profile", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> updateProfile(@Valid @RequestPart(name = "meta") FullInformationForm fullProfileForm,
                                                @RequestPart(name = "file") MultipartFile pictureFile,
                                                @AuthenticationPrincipal User authenticatedUser) {
        try {
            UserProfile updatedProfile = profileService.createFullProfileFor(fullProfileForm, authenticatedUser, pictureFile);
            return ResponseEntity.ok(updatedProfile);
        }
        catch (IOException ioException) {
            return ResponseEntity.internalServerError().body("An unexpected error occured:\n " + ioException.getMessage());
        }
    }

    @PatchMapping("/patch/page_size")
    @ResponseStatus(HttpStatus.OK)
    public UserProfile updatePageSize(@RequestBody int newPageSize,
                                      @AuthenticationPrincipal User authenticatedUser) {
        if(authenticatedUser.getProfile() == null){
            throw new RuntimeException("No profile found for your account");
        }
        return profileService.changePageSize(authenticatedUser.getProfile(), newPageSize);
    }

    @PatchMapping("/patch/header/{header_id}")
    @ResponseStatus(HttpStatus.OK)
    public Header updateProfileHeader(@Valid @RequestBody HeaderForm headerForm,
                                      @AuthenticationPrincipal User authenticatedUser, @PathVariable UUID header_id) {
        if(authenticatedUser.getProfile() == null){
            throw new RuntimeException("No profile found for your account");
        }
        return profileService.updateProfileHeader(headerForm, header_id, authenticatedUser);
    }

    @PatchMapping("/patch/education/{education_id}")
    @ResponseStatus(HttpStatus.OK)
    public Education updateProfileEducation(@Valid @RequestBody EducationForm educationForm,
                                            @AuthenticationPrincipal User authenticatedUser, @PathVariable UUID education_id) {
        if(authenticatedUser.getProfile() == null){
            throw new RuntimeException("No profile found for your account");
        }
        UserProfile profile = authenticatedUser.getProfile();
        return profileService.updateProfileEducation(educationForm, education_id, authenticatedUser);
    }

    @PatchMapping("/patch/experiences")
    @ResponseStatus(HttpStatus.OK)
    public UserProfile updateProfileExperiences(@Valid @RequestBody List<ExperienceForm>
                                                                  experienceFormList,
                                                   @AuthenticationPrincipal User authenticatedUser) {
        if(authenticatedUser.getProfile() == null){
            throw new RuntimeException("No profile found for your account");
        }
        UserProfile profile = authenticatedUser.getProfile();
        return profileService.updateProfileExperiences(profile, experienceFormList);
    }

    @DeleteMapping("/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfile(@AuthenticationPrincipal User deletingUser){
        if(deletingUser.getProfile() == null){
            throw new RuntimeException("No profile found for your account");
        }
        profileService.deleteProfile(deletingUser.getProfile().getId());
    }

    @DeleteMapping("/delete/header")
    @ResponseStatus(HttpStatus.OK)
    public UserProfile deleteProfileHeader(@AuthenticationPrincipal User deletingUser){
        if(deletingUser.getProfile() == null){
            throw new RuntimeException("No profile found for your account");
        }
        return profileService.deleteProfileHeader(deletingUser.getProfile());
    }

    @DeleteMapping("/delete/education")
    @ResponseStatus(HttpStatus.OK)
    public UserProfile deleteProfileEducation(@AuthenticationPrincipal User deletingUser){
        if(deletingUser.getProfile() == null){
            throw new RuntimeException("No profile found for your account");
        }
        return profileService.deleteProfileEducation(deletingUser.getProfile());
    }

    @DeleteMapping("/delete/experiences")
    @ResponseStatus(HttpStatus.OK)
    public UserProfile deleteProfileExperiences(@AuthenticationPrincipal User deletingUser){
        if(deletingUser.getProfile() == null){
            throw new RuntimeException("No profile found for your account");
        }
        return profileService.deleteProfileExperiences(deletingUser.getProfile());
    }

    @DeleteMapping("/delete/experiences/{experience_id}")
    @ResponseStatus(HttpStatus.OK)
    public UserProfile deleteSpecificExperience(@AuthenticationPrincipal User deletingUser,
                                                @PathVariable UUID experience_id){
        if(deletingUser.getProfile() == null){
            throw new RuntimeException("No profile found for your account");
        }
        return profileService.deleteSpecificProfileExperience(deletingUser.getProfile(),
                experience_id);
    }

    @PutMapping(value = "/update_image", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> changeImage(@AuthenticationPrincipal User changingUser,
                                   @RequestPart(name = "file") MultipartFile pictureFile)
    {
        try {
            UserProfile profile = changingUser.getProfile();
            userService.modifyProfilePictureOf(profile, pictureFile);
            return ResponseEntity.ok().body(profile);
        }

        catch (IOException ioException) {
            return ResponseEntity.internalServerError().body("An unexpected error occured:\n " + ioException.getMessage());
        }
    }

}

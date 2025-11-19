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
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    @Autowired
    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping(value = "/update_profile", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> updateProfile(@Valid @RequestPart(name = "meta") FullInformationForm fullProfileForm,
                                           @RequestPart(name = "file") MultipartFile pictureFile,
                                           @AuthenticationPrincipal User authenticatedUser) {
        try {
            UserProfile updatedProfile = profileService.createFullProfileFor
                    (fullProfileForm, authenticatedUser, pictureFile, false);
            return ResponseEntity.ok(updatedProfile);
        }
        catch (IOException ioException) {
            return ResponseEntity.internalServerError().body("An unexpected error occurred:\n " + ioException.getMessage());
        }
    }

    @PatchMapping("/patch/page_size/{profile_id}")
    @ResponseStatus(HttpStatus.OK)
    public UserProfile updatePageSize(@RequestBody int newPageSize,
                                      @AuthenticationPrincipal User authenticatedUser, @PathVariable UUID profile_id) {
        return profileService.changePageSize(authenticatedUser, profile_id, newPageSize);
    }

    @PatchMapping("/patch/header/{profile_id}/{header_id}")
    @ResponseStatus(HttpStatus.OK)
    public Header updateProfileHeader(@Valid @RequestBody HeaderForm headerForm,
                                      @AuthenticationPrincipal User authenticatedUser,
                                      @PathVariable UUID header_id, @PathVariable UUID profile_id) {
        return profileService.updateProfileHeader(headerForm, header_id, profile_id, authenticatedUser);
    }

    @PatchMapping("/patch/education/{profile_id}/{education_id}")
    @ResponseStatus(HttpStatus.OK)
    public Education updateProfileEducation(@Valid @RequestBody EducationForm educationForm,
                                            @AuthenticationPrincipal User authenticatedUser,
                                            @PathVariable UUID education_id, @PathVariable UUID profile_id) {
        return profileService.updateProfileEducation(educationForm, education_id, profile_id, authenticatedUser);
    }

    @PatchMapping("/patch/experiences/{profile_id}")
    @ResponseStatus(HttpStatus.OK)
    public UserProfile updateProfileExperiences(@Valid @RequestBody List<ExperienceForm>
                                                                  experienceFormList,
                                                   @AuthenticationPrincipal User authenticatedUser,
                                                @PathVariable UUID profile_id) {
        if(authenticatedUser.getProfile() == null){
            throw new RuntimeException("No profile found for your account");
        }
        UserProfile profile = authenticatedUser.getProfile();
        return profileService.updateProfileExperiences(profile, experienceFormList);
    }

    @DeleteMapping("/delete/{profile_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfile(@AuthenticationPrincipal User deletingUser, @PathVariable UUID profile_id) {
        profileService.deleteProfile(deletingUser, profile_id);
    }

    @DeleteMapping("/delete/header/{profile_id}")
    @ResponseStatus(HttpStatus.OK)
    public UserProfile deleteProfileHeader(@AuthenticationPrincipal User deletingUser, @PathVariable UUID profile_id) {
        return profileService.deleteProfileHeader(deletingUser, profile_id);
    }

    @DeleteMapping("/delete/education/{profile_id}")
    @ResponseStatus(HttpStatus.OK)
    public UserProfile deleteProfileEducation(@AuthenticationPrincipal User deletingUser,  @PathVariable UUID profile_id) {
        return profileService.deleteProfileEducation(deletingUser, profile_id);
    }

    @DeleteMapping("/delete/experiences/{profile_id}")
    @ResponseStatus(HttpStatus.OK)
    public UserProfile deleteProfileExperiences(@AuthenticationPrincipal User deletingUser, @PathVariable UUID profile_id) {

        return profileService.deleteProfileExperiences(deletingUser, profile_id);
    }

    @DeleteMapping("/delete/experiences/{profile_id}/{experience_id}")
    @ResponseStatus(HttpStatus.OK)
    public UserProfile deleteSpecificExperience(@AuthenticationPrincipal User deletingUser,
                                                @PathVariable UUID experience_id, @PathVariable UUID profile_id) {
        return profileService.deleteSpecificProfileExperience(deletingUser, profile_id,
                experience_id);
    }

    @PutMapping(value = "/update_image/{profile_id}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> changeImage(@AuthenticationPrincipal User changingUser,
                                   @RequestPart(name = "file") MultipartFile pictureFile,
                                         @PathVariable UUID profile_id)
    {
        try {
            UserProfile profile = profileService.modifyProfilePictureOf(changingUser, profile_id, pictureFile);
            return ResponseEntity.ok().body(profile);
        }

        catch (IOException ioException) {
            return ResponseEntity.internalServerError().body("An unexpected error occured:\n " + ioException.getMessage());
        }
    }

}

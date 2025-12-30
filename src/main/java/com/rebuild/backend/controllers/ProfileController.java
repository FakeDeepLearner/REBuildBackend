package com.rebuild.backend.controllers;

import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.forms.resume_forms.*;
import com.rebuild.backend.service.user_services.ProfileService;
import com.rebuild.backend.service.user_services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/api/profile")
@CacheConfig(cacheManager = "cacheManager", cacheNames = "profile_cache",
    keyGenerator = "profileCacheKeyGenerator")
public class ProfileController {

    private final ProfileService profileService;

    private final UserService userService;

    @Autowired
    public ProfileController(ProfileService profileService, UserService userService) {
        this.profileService = profileService;
        this.userService = userService;
    }

    @PostMapping(value = "/update_profile", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseStatus(HttpStatus.OK)
    @CacheEvict
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

    @PatchMapping("/patch/page_size")
    @ResponseStatus(HttpStatus.OK)
    @CacheEvict
    public UserProfile updatePageSize(@RequestBody int newPageSize,
                                      @AuthenticationPrincipal User authenticatedUser) {
        return profileService.changePageSize(authenticatedUser, newPageSize);
    }

    @PutMapping("/patch/header")
    @ResponseStatus(HttpStatus.OK)
    @CacheEvict
    public UserProfile updateProfileHeader(@Valid @RequestBody HeaderForm headerForm,
                                      @AuthenticationPrincipal User authenticatedUser) {
        return profileService.updateProfileHeader(headerForm, authenticatedUser);
    }

    @PutMapping("/patch/education/")
    @ResponseStatus(HttpStatus.OK)
    @CacheEvict
    public UserProfile updateProfileEducation(@Valid @RequestBody EducationForm educationForm,
                                            @AuthenticationPrincipal User authenticatedUser) {
        return profileService.updateProfileEducation(educationForm, authenticatedUser);
    }

    @PutMapping("/patch/experience/{experience_id}")
    @CacheEvict
    public UserProfile updateProfileExperience(@PathVariable UUID experience_id,
                                              @Valid @RequestBody ExperienceForm experienceForm,
                                              @AuthenticationPrincipal User authenticatedUser)
    {
        return profileService.updateProfileExperience(experienceForm, authenticatedUser, experience_id);
    }


    @PutMapping("/patch/experiences")
    @ResponseStatus(HttpStatus.OK)
    @CacheEvict
    public UserProfile updateProfileExperiences(@Valid @RequestBody List<ExperienceForm>
                                                                  experienceFormList,
                                                   @AuthenticationPrincipal User authenticatedUser) {
        return profileService.updateProfileExperiences(authenticatedUser, experienceFormList);
    }

    @DeleteMapping("/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @CacheEvict
    public void deleteProfile(@AuthenticationPrincipal User deletingUser) {
        profileService.deleteProfile(deletingUser);
    }

    @DeleteMapping("/delete/header")
    @ResponseStatus(HttpStatus.OK)
    @CacheEvict
    public UserProfile deleteProfileHeader(@AuthenticationPrincipal User deletingUser) {
        return profileService.deleteProfileHeader(deletingUser);
    }

    @DeleteMapping("/delete/education/")
    @CacheEvict
    @ResponseStatus(HttpStatus.OK)
    public UserProfile deleteProfileEducation(@AuthenticationPrincipal User deletingUser) {
        return profileService.deleteProfileEducation(deletingUser);
    }

    @DeleteMapping("/delete/experiences")
    @CacheEvict
    @ResponseStatus(HttpStatus.OK)
    public UserProfile deleteProfileExperiences(@AuthenticationPrincipal User deletingUser) {

        return profileService.deleteProfileExperiences(deletingUser);
    }

    @DeleteMapping("/delete/experiences/{experience_id}")
    @CacheEvict
    @ResponseStatus(HttpStatus.OK)
    public UserProfile deleteSpecificExperience(@AuthenticationPrincipal User deletingUser,
                                                @PathVariable UUID experience_id) {
        return profileService.deleteSpecificProfileExperience(deletingUser,
                experience_id);
    }

    @PutMapping(value = "/update_image", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseStatus(HttpStatus.OK)
    @CacheEvict
    public ResponseEntity<?> changeImage(@AuthenticationPrincipal User changingUser,
                                   @RequestPart(name = "file") MultipartFile pictureFile)
    {
        try {
            UserProfile profile = profileService.modifyProfilePictureOf(changingUser, pictureFile);
            return ResponseEntity.ok().body(profile);
        }

        catch (IOException ioException) {
            return ResponseEntity.internalServerError().body("An unexpected error occurred:\n " +
                    ioException.getMessage());
        }
    }

    @DeleteMapping("/delete_phone")
    @ResponseStatus(NO_CONTENT)
    public void removePhoneNumber(@AuthenticationPrincipal User authenticatedUser) {
        userService.removePhoneOf(authenticatedUser);
    }
}

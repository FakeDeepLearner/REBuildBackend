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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public UserProfile createFullProfileFor(@Valid @RequestBody FullInformationForm fullProfileForm,
                                        @AuthenticationPrincipal User authenticatedUser) {
        return profileService.createFullProfileFor(fullProfileForm, authenticatedUser);
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
        return profileService.updateProfileHeader(headerForm, header_id);
    }

    @PatchMapping("/patch/education/{education_id}")
    @ResponseStatus(HttpStatus.OK)
    public Education updateProfileEducation(@Valid @RequestBody EducationForm educationForm,
                                            @AuthenticationPrincipal User authenticatedUser, @PathVariable UUID education_id) {
        if(authenticatedUser.getProfile() == null){
            throw new RuntimeException("No profile found for your account");
        }
        UserProfile profile = authenticatedUser.getProfile();
        return profileService.updateProfileEducation(educationForm, education_id);
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

    @PatchMapping("/patch/sections")
    @ResponseStatus(HttpStatus.OK)
    public UserProfile updateProfileSections(@Valid @RequestBody List<SectionForm> sectionFormList,
                                             @AuthenticationPrincipal User authenticatedUser){
        if(authenticatedUser.getProfile() == null){
            throw new RuntimeException("No profile found for your account");
        }
        UserProfile profile = authenticatedUser.getProfile();
        return profileService.updateProfileSections(profile, sectionFormList);
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

    @DeleteMapping("/delete/sections")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public UserProfile deleteProfileSections(@AuthenticationPrincipal User deletingUser){
        if(deletingUser.getProfile() == null){
            throw new RuntimeException("No profile found for your account");
        }
        return profileService.deleteProfileSections(deletingUser.getProfile());
    }

    @DeleteMapping("/delete/sections/{section_id}")
    @ResponseStatus(HttpStatus.OK)
    public UserProfile deleteSpecificSection(@AuthenticationPrincipal User deletingUser,
                                                @PathVariable UUID section_id){
        if(deletingUser.getProfile() == null){
            throw new RuntimeException("No profile found for your account");
        }
        return profileService.deleteSpecificSection(deletingUser.getProfile(),
                section_id);
    }

    @PutMapping("/update_profile")
    @ResponseStatus(HttpStatus.OK)
    public UserProfile changeEntireProfile(@AuthenticationPrincipal User updatingUser,
                                           @Valid @RequestBody FullInformationForm profileForm){
        return profileService.updateEntireProfile(updatingUser.getProfile(), profileForm);

    }
}

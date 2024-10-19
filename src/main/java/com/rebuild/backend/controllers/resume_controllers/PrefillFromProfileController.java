package com.rebuild.backend.controllers.resume_controllers;

import com.rebuild.backend.exceptions.profile_exceptions.NoAttributeInProfileException;
import com.rebuild.backend.exceptions.profile_exceptions.NoProfileException;
import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.entities.profile_entities.ProfileEducation;
import com.rebuild.backend.model.entities.profile_entities.ProfileHeader;
import com.rebuild.backend.service.resume_services.ResumeService;
import com.rebuild.backend.utils.converters.ProfileObjectConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile/prefill")
public class PrefillFromProfileController {
    private final ProfileObjectConverter profileObjectConverter;

    private final ResumeService resumeService;

    @Autowired
    public PrefillFromProfileController(ProfileObjectConverter profileObjectConverter,
                                        ResumeService resumeService) {
        this.profileObjectConverter = profileObjectConverter;
        this.resumeService = resumeService;
    }

    @GetMapping("/header/{resume_id}")
    @ResponseStatus(HttpStatus.OK)
    public Resume prefillHeader(@PathVariable UUID resume_id){
        Resume associatedResume = resumeService.findById(resume_id);
        User resumeUser = associatedResume.getUser();
        if(resumeUser.getProfile() == null){
            throw new NoProfileException("You haven't set your profile yet, this operation can't be completed");
        }
        if(resumeUser.getProfile().getHeader() == null){
            throw new NoAttributeInProfileException("Your profile does not have a header set");
        }
        ProfileHeader originalHeader = resumeUser.getProfile().getHeader();
        Header transformedHeader = profileObjectConverter.convertToHeader(originalHeader);
        return resumeService.setHeader(associatedResume, transformedHeader);
    }

    @GetMapping("/education/{resume_id}")
    @ResponseStatus(HttpStatus.OK)
    public Resume prefillEducation(@PathVariable UUID resume_id){
        Resume associatedResume = resumeService.findById(resume_id);
        User resumeUser = associatedResume.getUser();
        if(resumeUser.getProfile() == null){
            throw new NoProfileException("You haven't set your profile yet, this operation can't be completed");
        }
        if(resumeUser.getProfile().getEducation() == null){
            throw new NoAttributeInProfileException("Your profile does not have an education set");
        }
        ProfileEducation originalEducation = resumeUser.getProfile().getEducation();
        Education transformedEducation = profileObjectConverter.convertToEducation(originalEducation);
        return resumeService.setEducation(associatedResume, transformedEducation);
    }

    @GetMapping("/experiences/{resume_id}")
    @ResponseStatus(HttpStatus.OK)
    public Resume prefillExperience(@PathVariable UUID resume_id){
        Resume associatedResume = resumeService.findById(resume_id);
        User resumeUser = associatedResume.getUser();
        if(resumeUser.getProfile() == null){
            throw new NoProfileException("You haven't set your profile yet, this operation can't be completed");
        }
        if(resumeUser.getProfile().getExperienceList() == null){
            throw new NoAttributeInProfileException("Your profile does not have experiences set");
        }
        List<Experience> convertedExperiences = resumeUser.getProfile().getExperienceList().
                stream().map(profileObjectConverter::convertToExperience).
                toList();
        return resumeService.setExperiences(associatedResume, convertedExperiences);
    }

    @GetMapping("/sections/{resume_id}")
    @ResponseStatus(HttpStatus.OK)
    public Resume prefillSections(@PathVariable UUID resume_id){
        Resume associatedResume = resumeService.findById(resume_id);
        User resumeUser = associatedResume.getUser();
        if(resumeUser.getProfile() == null){
            throw new NoProfileException("You haven't set your profile yet, this operation can't be completed");
        }
        if(resumeUser.getProfile().getSections() == null){
            throw new NoAttributeInProfileException("Your profile does not have sections set");
        }

        List<ResumeSection> convertedSections = resumeUser.getProfile().getSections().
                stream().map(profileObjectConverter::convertToSection).
                toList();
        return resumeService.setSections(associatedResume, convertedSections);
    }
}

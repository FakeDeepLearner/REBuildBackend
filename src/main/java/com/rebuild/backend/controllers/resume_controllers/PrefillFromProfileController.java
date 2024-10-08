package com.rebuild.backend.controllers.resume_controllers;

import com.rebuild.backend.exceptions.profile_exceptions.NoAttributeInProfileException;
import com.rebuild.backend.exceptions.profile_exceptions.NoProfileException;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.entities.profile_entities.ProfileEducation;
import com.rebuild.backend.model.entities.profile_entities.ProfileHeader;
import com.rebuild.backend.model.entities.resume_entities.Education;
import com.rebuild.backend.model.entities.resume_entities.Experience;
import com.rebuild.backend.model.entities.resume_entities.Header;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.service.resume_services.ResumeService;
import com.rebuild.backend.utils.converters.ProfileObjectConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        return resumeService.setHeader(resume_id, transformedHeader);
    }

    @GetMapping("/education/{resume_id}")
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
        return resumeService.setEducation(resume_id, transformedEducation);
    }

    @GetMapping("/experiences/{resume_id}")
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
        return resumeService.setExperiences(resume_id, convertedExperiences);
    }
}

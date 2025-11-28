package com.rebuild.backend.controllers.resume_controllers;

import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.repository.resume_repositories.ResumeRepository;
import com.rebuild.backend.service.resume_services.ResumeService;
import com.rebuild.backend.utils.converters.ObjectConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile/prefill")
public class PrefillFromProfileController {
    private final ObjectConverter objectConverter;

    private final ResumeService resumeService;

    private final ResumeRepository resumeRepository;

    @Autowired
    public PrefillFromProfileController(ObjectConverter objectConverter,
                                        ResumeService resumeService, ResumeRepository resumeRepository) {
        this.objectConverter = objectConverter;
        this.resumeService = resumeService;
        this.resumeRepository = resumeRepository;
    }

    @GetMapping("/header/{resume_id}")
    @ResponseStatus(HttpStatus.OK)
    @CacheEvict(value = "resume_cache", key = "#user.id.toString() + ':' + #resume_id")
    public Resume prefillHeader(@PathVariable UUID resume_id,
                                @AuthenticationPrincipal User user){
        Resume associatedResume = resumeService.findByUserIndex(user, resume_id);
        User resumeUser = associatedResume.getUser();
        if(resumeUser.getProfile() == null){
            throw new RuntimeException("You haven't set your profile yet, this operation can't be completed");
        }
        if(resumeUser.getProfile().getHeader() == null){
            throw new RuntimeException("Your profile does not have a header set");
        }
        Header originalHeader = resumeUser.getProfile().getHeader();
        Header transformedHeader = objectConverter.convertToHeader(originalHeader);
        resumeService.setHeader(associatedResume, transformedHeader);
        return resumeRepository.save(associatedResume);
    }

    @GetMapping("/education/{resume_id}")
    @ResponseStatus(HttpStatus.OK)
    @CacheEvict(value = "resume_cache", key = "#user.id.toString() + ':' + #resume_id")
    public Resume prefillEducation(@PathVariable UUID resume_id,
                                   @AuthenticationPrincipal User user){
        Resume associatedResume = resumeService.findByUserIndex(user, resume_id);
        User resumeUser = associatedResume.getUser();
        if(resumeUser.getProfile() == null){
            throw new RuntimeException("You haven't set your profile yet, this operation can't be completed");
        }
        if(resumeUser.getProfile().getEducation() == null){
            throw new RuntimeException("Your profile does not have an education set");
        }
        Education originalEducation = resumeUser.getProfile().getEducation();
        Education transformedEducation = objectConverter.convertToEducation(originalEducation);
        resumeService.setEducation(associatedResume, transformedEducation);
        return resumeRepository.save(associatedResume);
    }

    @GetMapping("/experiences/{resume_id}")
    @ResponseStatus(HttpStatus.OK)
    @CacheEvict(value = "resume_cache", key = "#user.id.toString() + ':' + #resume_id")
    public Resume prefillExperience(@PathVariable UUID resume_id,
                                               @AuthenticationPrincipal User user){
        Resume associatedResume = resumeService.findByUserIndex(user, resume_id);
        User resumeUser = associatedResume.getUser();
        if(resumeUser.getProfile() == null){
            throw new RuntimeException("You haven't set your profile yet, this operation can't be completed");
        }
        if(resumeUser.getProfile().getExperienceList() == null){
            throw new RuntimeException("Your profile does not have experiences set");
        }
        List<Experience> convertedExperiences = resumeUser.getProfile().getExperienceList().
                stream().map(objectConverter::convertToExperience).peek(experience -> {
                    experience.setResume(associatedResume);
                }).
                toList();
        resumeService.setExperiences(associatedResume, convertedExperiences);
        return resumeRepository.save(associatedResume);

    }
}

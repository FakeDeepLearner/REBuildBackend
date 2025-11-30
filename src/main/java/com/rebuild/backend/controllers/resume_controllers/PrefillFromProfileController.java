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

    private final ResumeService resumeService;

    @Autowired
    public PrefillFromProfileController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @GetMapping("/header/{resume_id}")
    @ResponseStatus(HttpStatus.OK)
    @CacheEvict(value = "resume_cache", key = "#user.id.toString() + ':' + #resume_id")
    public Resume prefillHeader(@PathVariable UUID resume_id,
                                @AuthenticationPrincipal User user){
        return resumeService.prefillHeader(resume_id, user);
    }

    @GetMapping("/education/{resume_id}")
    @ResponseStatus(HttpStatus.OK)
    @CacheEvict(value = "resume_cache", key = "#user.id.toString() + ':' + #resume_id")
    public Resume prefillEducation(@PathVariable UUID resume_id,
                                   @AuthenticationPrincipal User user){
       return resumeService.prefillEducation(resume_id, user);
    }

    @GetMapping("/experiences/{resume_id}")
    @ResponseStatus(HttpStatus.OK)
    @CacheEvict(value = "resume_cache", key = "#user.id.toString() + ':' + #resume_id")
    public Resume prefillExperience(@PathVariable UUID resume_id,
                                               @AuthenticationPrincipal User user){
        return resumeService.prefillExperiencesList(resume_id, user);
    }
}

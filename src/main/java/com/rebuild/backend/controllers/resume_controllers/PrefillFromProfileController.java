package com.rebuild.backend.controllers.resume_controllers;

import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.service.resume_services.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    @CacheEvict(cacheManager = "cacheManager", value = "resume_cache", keyGenerator = "resumeCacheKeyGenerator")
    public Resume prefillHeader(@PathVariable UUID resume_id,
                                @AuthenticationPrincipal User user){
        return resumeService.prefillHeader(resume_id, user);
    }

    @GetMapping("/education/{resume_id}")
    @ResponseStatus(HttpStatus.OK)
    @CacheEvict(cacheManager = "cacheManager", value = "resume_cache", keyGenerator = "resumeCacheKeyGenerator")
    public Resume prefillEducation(@PathVariable UUID resume_id,
                                   @AuthenticationPrincipal User user){
       return resumeService.prefillEducation(resume_id, user);
    }

    @GetMapping("/experiences/{resume_id}")
    @ResponseStatus(HttpStatus.OK)
    @CacheEvict(cacheManager = "cacheManager", value = "resume_cache", keyGenerator = "resumeCacheKeyGenerator")
    public Resume prefillExperience(@PathVariable UUID resume_id,
                                               @AuthenticationPrincipal User user){
        return resumeService.prefillExperiencesList(resume_id, user);
    }
}

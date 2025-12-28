package com.rebuild.backend.controllers.resume_controllers;

import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.service.resume_services.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/resume/delete", method = RequestMethod.DELETE)
@ResponseStatus(HttpStatus.OK)
public class ResumeDeleteController {

    private final ResumeService resumeService;

    @Autowired
    public ResumeDeleteController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @DeleteMapping("/header/{resume_id}")
    @CacheEvict(cacheManager = "cacheManager", value = "resume_cache", keyGenerator = "resumeCacheKeyGenerator")
    public Resume deleteHeader(@PathVariable UUID resume_id,
                               @AuthenticationPrincipal User user) {
        return resumeService.deleteHeader(user, resume_id);
    }

    @DeleteMapping("/experience/{resume_id}/{experience_id}")
    @CacheEvict(cacheManager = "cacheManager", value = "resume_cache", key = "#user.id.toString() + ':' + #resume_id")
    public Resume deleteExperience(@PathVariable UUID experience_id, @PathVariable UUID resume_id,
                                   @AuthenticationPrincipal User user) {
        return resumeService.deleteExperience(user, resume_id, experience_id);
    }

    @DeleteMapping("/education/{resume_id}")
    @CacheEvict(cacheManager = "cacheManager", value = "resume_cache", keyGenerator = "resumeCacheKeyGenerator")
    public Resume deleteEducation(@PathVariable UUID resume_id, @AuthenticationPrincipal User user) {
        return resumeService.deleteEducation(user, resume_id);
    }

    @DeleteMapping("/{resume_id}")
    @CacheEvict(cacheManager = "cacheManager", value = "resume_cache", keyGenerator = "resumeCacheKeyGenerator")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteResume(@PathVariable UUID resume_id, @AuthenticationPrincipal User user) {
        resumeService.deleteById(user, resume_id);
    }
}

package com.rebuild.backend.controllers.resume_controllers;

import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.forms.resume_forms.ResumeCreationForm;
import com.rebuild.backend.service.resume_services.ResumeService;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.*;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;

@RestController("/api/utils")
public class ResumeUtilController {

    private final ResumeService resumeService;

    public ResumeUtilController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PutMapping("/change_name/{resume_id}")
    @CacheEvict(cacheManager = "cacheManager", cacheNames = "resume_cache",
            keyGenerator = "resumeCacheKeyGenerator")
    @ResponseStatus(HttpStatus.OK)
    public Resume changeResumeName(@RequestBody String newName,
                                              @PathVariable UUID resume_id,
                                              @AuthenticationPrincipal User user) {
        return resumeService.changeName(user, resume_id, newName);
    }

    @PostMapping("/copy/{resume_id}")
    @ResponseStatus(HttpStatus.OK)
    public Resume copyResume(@RequestBody String newName,
                                        @AuthenticationPrincipal User user, @PathVariable UUID resume_id) {
        return resumeService.copyResume(user, resume_id, newName);
    }

    @GetMapping("/view_experience_values")
    public Collection<String> getPossibleExperienceValues()
    {
        return Arrays.asList(
                "Full Time", "Part Time",
                "Casual", "Volunteer",
                "Internship", "Contract",
                "Freelance", "Self-Employed"
        );
    }
}

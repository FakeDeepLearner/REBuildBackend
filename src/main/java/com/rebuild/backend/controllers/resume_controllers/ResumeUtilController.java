package com.rebuild.backend.controllers.resume_controllers;

import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.users.User;
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
    @CacheEvict(cacheManager = "cacheManager", value = "resume_cache", keyGenerator = "resumeCacheKeyGenerator")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> changeResumeName(@RequestBody String newName,
                                              @PathVariable UUID resume_id,
                                              @AuthenticationPrincipal User user) {
        try {
            Resume changedResume = resumeService.changeName(user, resume_id, newName);
            return ResponseEntity.ok(changedResume);
        }

        catch (DataIntegrityViolationException e){
            Throwable cause = e.getCause();

            if(cause instanceof ConstraintViolationException violationException &&
                    Objects.equals(violationException.getConstraintName(), "uk_same_user_resume_name")){
                return ResponseEntity.status(CONFLICT).body("You already have a resume with this name");
            }
        }

        return null;
    }

    @PostMapping("/copy/{resume_id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> copyResume(@RequestBody ResumeCreationForm creationForm,
                                        @AuthenticationPrincipal User user, @PathVariable UUID resume_id) {
        try {
            Resume copiedResume = resumeService.copyResume(user, resume_id, creationForm);
            return ResponseEntity.ok(copiedResume);
        }

        catch (DataIntegrityViolationException e){
            Throwable cause = e.getCause();

            if(cause instanceof ConstraintViolationException violationException &&
                    Objects.equals(violationException.getConstraintName(), "uk_same_user_resume_name")){
                return ResponseEntity.status(CONFLICT).body("You already have a resume with this name");
            }
        }

        catch (RuntimeException e){
            return ResponseEntity.status(BAD_REQUEST).body(e.getMessage());
        }
        return null;
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

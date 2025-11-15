package com.rebuild.backend.controllers.resume_controllers;

import com.rebuild.backend.model.entities.resume_entities.ExperienceType;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.forms.resume_forms.ResumeCreationForm;
import com.rebuild.backend.service.resume_services.ResumeService;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;

@RestController
public class ResumeUtilController {

    private final ResumeService resumeService;

    public ResumeUtilController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PutMapping("/api/resume/change_name/{resume_id}")
    @CacheEvict(value = "resume_cache", key = "#user.id.toString() + ':' + #resume_id")
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

    @PostMapping("/api/resume/copy/{resume_id}")
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

    @PostMapping("/api/download/{resume_id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> downloadResumeAsText(@AuthenticationPrincipal User user,
                                                       @PathVariable UUID resume_id,
                                                       @RequestBody boolean includeMetadata) {
        String resumeMetadata = "";

        Resume downloadingResume = resumeService.findByUserIndex(user, resume_id);
        if (includeMetadata) {
            resumeMetadata = "METADATA: \n" + "\tTime Created: " + downloadingResume.getCreationTime()
                    + "\n\tLast Modified Time: " + downloadingResume.getLastModifiedTime()
                    + "\n\t Download Time " + LocalDateTime.now() + "\n\n";
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", downloadingResume.getName());
        headers.setContentType(MediaType.TEXT_PLAIN);
        return ResponseEntity.status(200).headers(headers).body(resumeMetadata + downloadingResume);

    }

    @GetMapping("/api/view_experience_values")
    public Collection<String> getPossibleExperienceValues()
    {
        return Arrays.stream(ExperienceType.values()).
                map(ExperienceType::getStoredValue).
                toList();
    }
}

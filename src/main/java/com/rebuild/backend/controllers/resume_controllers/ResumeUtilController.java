package com.rebuild.backend.controllers.resume_controllers;

import com.rebuild.backend.model.entities.resume_entities.ExperienceType;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.service.resume_services.ResumeService;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;

@RestController
@SuppressWarnings("OptionalGetWithoutIsPresent")
public class ResumeUtilController {

    private final ResumeService resumeService;

    public ResumeUtilController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PutMapping("/api/resume/change_name{index}")
    @CacheEvict(value = "resume_cache", key = "#user.id.toString()" + "-" + "#index")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> changeResumeName(@RequestBody String newName,
                                              @PathVariable int index,
                                              @AuthenticationPrincipal User user) {
        try {
            Resume changedResume = resumeService.changeName(user, index, newName);
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

    @PostMapping("/api/resume/copy/{index}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> copyResume(@RequestBody String newName,
                                        @AuthenticationPrincipal User user, @PathVariable int index) {
        try {
            Resume copiedResume = resumeService.copyResume(user, index, newName);
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

    @GetMapping("/api/download/{index}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> downloadResumeAsText(@AuthenticationPrincipal User user,
                                                       @PathVariable int index,
                                                       @ModelAttribute boolean includeMetadata,
                                                       BindingResult result) {
        String resumeMetadata = "";

        Resume downloadingResume = resumeService.findByUserIndex(user, index);
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

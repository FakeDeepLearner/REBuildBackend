package com.rebuild.backend.controllers.resume_controllers;

import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.responses.resume_responses.ResumeResponse;
import com.rebuild.backend.service.resume_services.ResumeDeleteService;
import com.rebuild.backend.service.resume_services.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/resume/delete", method = RequestMethod.DELETE)
@ResponseStatus(HttpStatus.OK)
public class ResumeDeleteController {

    private final ResumeDeleteService deleteService;

    @Autowired
    public ResumeDeleteController(ResumeDeleteService deleteService) {
        this.deleteService = deleteService;
    }

    @DeleteMapping("/header/{resume_id}")
    public ResumeResponse deleteHeader(@PathVariable UUID resume_id,
                                       @AuthenticationPrincipal User user) {
        return deleteService.deleteHeader(user, resume_id);
    }

    @DeleteMapping("/experience/{resume_id}/{experience_id}")
    public ResumeResponse deleteExperience(@PathVariable UUID experience_id, @PathVariable UUID resume_id,
                                   @AuthenticationPrincipal User user) {
        return deleteService.deleteExperience(user, resume_id, experience_id);
    }

    @DeleteMapping("/education/{resume_id}")
    public ResumeResponse deleteEducation(@PathVariable UUID resume_id, @AuthenticationPrincipal User user) {
        return deleteService.deleteEducation(user, resume_id);
    }

    @DeleteMapping("/project/{resume_id}/{project_id}")
    public ResumeResponse deleteProject(@PathVariable UUID project_id, @PathVariable UUID resume_id,
                                @AuthenticationPrincipal User user) {
        return deleteService.deleteProject(user, project_id, resume_id);
    }

    @DeleteMapping("/experiences/{resume_id}")
    public ResumeResponse deleteAllExperiences(@AuthenticationPrincipal User user, @PathVariable UUID resume_id) {
        return deleteService.deleteAllExperiences(user, resume_id);
    }

    @DeleteMapping("/projects/{resume_id}")
    public ResumeResponse deleteAllProjects(@AuthenticationPrincipal User user, @PathVariable UUID resume_id) {
        return deleteService.deleteAllProjects(user, resume_id);
    }

    @DeleteMapping("/{resume_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteResume(@PathVariable UUID resume_id, @AuthenticationPrincipal User user) {
        deleteService.deleteById(user, resume_id);
    }
}

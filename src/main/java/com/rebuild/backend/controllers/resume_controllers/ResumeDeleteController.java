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

    @DeleteMapping("/header/{id}/{index}")
    @CacheEvict(value = "resume_cache", key = "#user.id.toString()" + "-" + "#index")
    public Resume deleteHeader(@PathVariable UUID id, @PathVariable int index,
                               @AuthenticationPrincipal User user) {
        return resumeService.deleteHeader(id);
    }

    @DeleteMapping("/experience/{resID}/{expID}/{index}")
    @CacheEvict(value = "resume_cache", key = "#user.id.toString()" + "-" + "#index")
    public Resume deleteExperience(@PathVariable UUID resID, @PathVariable UUID expID,
                                   @PathVariable int index,
                                   @AuthenticationPrincipal User user) {
        return resumeService.deleteExperience(resID, expID);
    }

    @DeleteMapping("/education/{id}/{index}")
    @CacheEvict(value = "resume_cache", key = "#user.id.toString()" + "-" + "#index")
    public Resume deleteEducation(@PathVariable UUID id,
                                  @PathVariable int index, @AuthenticationPrincipal User user) {
        return resumeService.deleteEducation(id);
    }
}

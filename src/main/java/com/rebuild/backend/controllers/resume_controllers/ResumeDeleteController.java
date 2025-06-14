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

    @DeleteMapping("/header/{index}")
    @CacheEvict(value = "resume_cache", key = "#user.id.toString()" + "-" + "#index")
    public Resume deleteHeader(@PathVariable int index,
                               @AuthenticationPrincipal User user) {
        return resumeService.deleteHeader(user, index);
    }

    @DeleteMapping("/experience/{resume_index}/{experience_index}")
    @CacheEvict(value = "resume_cache", key = "#user.id.toString()" + "-" + "#resume_index")
    public Resume deleteExperience(@PathVariable int resume_index, @PathVariable int experience_index,
                                   @AuthenticationPrincipal User user) {
        return resumeService.deleteExperience(user, resume_index, experience_index);
    }

    @DeleteMapping("/education{index}")
    @CacheEvict(value = "resume_cache", key = "#user.id.toString()" + "-" + "#index")
    public Resume deleteEducation(@PathVariable int index, @AuthenticationPrincipal User user) {
        return resumeService.deleteEducation(user, index);
    }
}

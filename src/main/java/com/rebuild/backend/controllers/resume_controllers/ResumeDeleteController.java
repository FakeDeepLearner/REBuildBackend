package com.rebuild.backend.controllers.resume_controllers;

import com.rebuild.backend.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/delete")
@ResponseStatus(HttpStatus.NO_CONTENT)
public class ResumeDeleteController {

    private final ResumeService resumeService;

    @Autowired
    public ResumeDeleteController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @DeleteMapping("/header/{id}")
    public void deleteHeader(@PathVariable UUID id){
        resumeService.deleteHeader(id);
    }

    @DeleteMapping("/experience/{resID}/{expID}")
    public void deleteExperience(@PathVariable UUID resID, @PathVariable UUID expID){
        resumeService.deleteExperience(resID, expID);
    }

    @DeleteMapping("/education/{id}")
    public void deleteEducation(@PathVariable UUID id){
        resumeService.deleteEducation(id);
    }
}

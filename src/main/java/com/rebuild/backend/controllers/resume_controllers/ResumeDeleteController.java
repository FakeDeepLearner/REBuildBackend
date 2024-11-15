package com.rebuild.backend.controllers.resume_controllers;

import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.service.resume_services.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    @DeleteMapping("/header/{id}")
    public Resume deleteHeader(@PathVariable UUID id){
        return resumeService.deleteHeader(id);
    }

    @DeleteMapping("/experience/{resID}/{expID}")
    public Resume deleteExperience(@PathVariable UUID resID, @PathVariable UUID expID){
        return resumeService.deleteExperience(resID, expID);
    }

    @DeleteMapping("/education/{id}")
    public Resume deleteEducation(@PathVariable UUID id){
        return resumeService.deleteEducation(id);
    }
}

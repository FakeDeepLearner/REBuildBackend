package com.rebuild.backend.controllers.resume_controllers;

import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.forms.resume_forms.FullResumeForm;
import com.rebuild.backend.service.resume_services.ResumeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class FullUpdateController {

    private final ResumeService resumeService;


    @Autowired
    public FullUpdateController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PutMapping("/api/put/{res_id}")
    @ResponseStatus(HttpStatus.OK)
    public Resume updateFullResume(@Valid @RequestBody FullResumeForm fullResumeForm,
                                                     @PathVariable UUID res_id){
        Resume associatedResume = resumeService.findById(res_id);
        return resumeService.fullUpdate(associatedResume, fullResumeForm);
    }
}

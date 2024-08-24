package com.rebuild.backend.controllers.resume_controllers;

import com.rebuild.backend.model.entities.resume_entities.Education;
import com.rebuild.backend.model.entities.resume_entities.Header;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.forms.resume_forms.FullResumeForm;
import com.rebuild.backend.model.responses.FullResumeUpdateResponse;
import com.rebuild.backend.service.ResumeService;
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
    public FullResumeUpdateResponse updateFullResume(@Valid @RequestBody FullResumeForm fullResumeForm,
                                                     @PathVariable UUID res_id){
        Header newHeader = resumeService.changeHeaderInfo(res_id, fullResumeForm.name(),
                fullResumeForm.email(),
                fullResumeForm.phoneNumber());

        Education newEducation = resumeService.changeEducationInfo(res_id,
                fullResumeForm.schoolName(),
                fullResumeForm.relevantCoursework());

        Resume resumeAfterNewExperiences =  resumeService.setExperiences(res_id, fullResumeForm.experiences());

        return new FullResumeUpdateResponse(res_id, newHeader, newEducation, resumeAfterNewExperiences.getExperiences());
    }
}

package com.rebuild.backend.controller;

import com.rebuild.backend.model.entities.Education;
import com.rebuild.backend.model.entities.Experience;
import com.rebuild.backend.model.entities.Header;
import com.rebuild.backend.model.forms.resume_forms.EducationForm;
import com.rebuild.backend.model.forms.resume_forms.ExperienceForm;
import com.rebuild.backend.model.forms.resume_forms.HeaderForm;
import com.rebuild.backend.service.ResumeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class ResumeFormController {
    private final ResumeService resumeService;

    @Autowired
    public ResumeFormController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }


    @PostMapping("/api/post/header/{res_id}")
    public Header createNewHeader(@PathVariable UUID res_id, @RequestBody HeaderForm headerForm){
        return resumeService.createNewHeader(res_id, headerForm.name(), headerForm.email(), headerForm.number());
    }

    @PostMapping("/api/post/experience/{res_id}")
    public Experience createNewExperience(@PathVariable UUID res_id, @RequestBody ExperienceForm experienceForm){
        return resumeService.createNewExperience(res_id, experienceForm.companyName(),
                experienceForm.timePeriod(), experienceForm.bullets());
    }

    @PostMapping("/api/post/education/{res_id}")
    public Education createNewEducation(@PathVariable UUID res_id, @RequestBody EducationForm educationForm){
        return resumeService.createNewEducation(res_id, educationForm.schoolName(),
                educationForm.relevantCoursework());
    }

    @PutMapping("/api/put/header/{res_id}")
    public Header modifyHeader(@PathVariable UUID res_id, @RequestBody HeaderForm headerForm){
        return resumeService.changeHeaderInfo(res_id, headerForm.name(),
                headerForm.email(), headerForm.number());
    }

    @PutMapping("/api/put/experience/{res_id}/{exp_id}")
    public Experience modifyExperience(@PathVariable UUID res_id, @PathVariable UUID exp_id,
                                       @RequestBody ExperienceForm experienceForm){
        return resumeService.changeExperienceInfo(res_id, exp_id, experienceForm.companyName(),
                experienceForm.timePeriod(), experienceForm.bullets());

    }

    @PutMapping("/api/put/education/{res_id}")
    public Education modifyEducation(@PathVariable UUID res_id, @RequestBody EducationForm educationForm){
        return resumeService.changeEducationInfo(res_id, educationForm.schoolName(),
                educationForm.relevantCoursework());
    }

}

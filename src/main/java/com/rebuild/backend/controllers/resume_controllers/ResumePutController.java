package com.rebuild.backend.controllers.resume_controllers;

import com.rebuild.backend.model.constraints.entities.Education;
import com.rebuild.backend.model.constraints.entities.Experience;
import com.rebuild.backend.model.constraints.entities.Header;
import com.rebuild.backend.model.forms.resume_forms.EducationForm;
import com.rebuild.backend.model.forms.resume_forms.ExperienceForm;
import com.rebuild.backend.model.forms.resume_forms.HeaderForm;
import com.rebuild.backend.service.ResumeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/put")
public class ResumePutController {
    private final ResumeService resumeService;

    @Autowired
    public ResumePutController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PutMapping("/header/{res_id}")
    public Header modifyHeader(@PathVariable UUID res_id, @Valid @RequestBody HeaderForm headerForm){
        return resumeService.changeHeaderInfo(res_id, headerForm.name(),
                headerForm.email(), headerForm.number());
    }

    @PutMapping("/experience/{res_id}/{exp_id}")
    public Experience modifyExperience(@PathVariable UUID res_id, @PathVariable UUID exp_id,
                                       @Valid @RequestBody ExperienceForm experienceForm){
        return resumeService.changeExperienceInfo(res_id, exp_id, experienceForm.companyName(),
                experienceForm.timePeriod(), experienceForm.bullets());

    }

    @PutMapping("/education/{res_id}")
    public Education modifyEducation(@PathVariable UUID res_id, @Valid @RequestBody EducationForm educationForm){
        return resumeService.changeEducationInfo(res_id, educationForm.schoolName(),
                educationForm.relevantCoursework());
    }
}

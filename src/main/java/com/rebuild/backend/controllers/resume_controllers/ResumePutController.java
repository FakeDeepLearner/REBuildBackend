package com.rebuild.backend.controllers.resume_controllers;

import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.forms.resume_forms.EducationForm;
import com.rebuild.backend.model.forms.resume_forms.ExperienceForm;
import com.rebuild.backend.model.forms.resume_forms.HeaderForm;
import com.rebuild.backend.service.resume_services.ResumeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/resume/put", method = RequestMethod.PUT)
@ResponseStatus(HttpStatus.OK)
public class ResumePutController {
    private final ResumeService resumeService;

    @Autowired
    public ResumePutController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PutMapping("/header/{res_id}")
    public Resume modifyHeader(@PathVariable UUID res_id, @Valid @RequestBody HeaderForm headerForm){
        return resumeService.changeHeaderInfo(res_id, headerForm.name(),
                headerForm.email(), headerForm.number());
    }

    @PutMapping("/experience/{res_id}/{exp_id}")
    public Resume modifyExperience(@PathVariable UUID res_id, @PathVariable UUID exp_id,
                                       @Valid @RequestBody ExperienceForm experienceForm){
        Duration duration = Duration.between(experienceForm.startDate(), experienceForm.endDate());
        return resumeService.changeExperienceInfo(res_id, exp_id, experienceForm.companyName(),
                experienceForm.technologies(), duration, experienceForm.bullets());

    }

    @PutMapping("/education/{res_id}")
    public Resume modifyEducation(@PathVariable UUID res_id, @Valid @RequestBody EducationForm educationForm){
        return resumeService.changeEducationInfo(res_id, educationForm.schoolName(),
                educationForm.relevantCoursework());
    }
}

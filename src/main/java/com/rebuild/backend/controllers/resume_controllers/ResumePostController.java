package com.rebuild.backend.controllers.resume_controllers;

import com.rebuild.backend.model.entities.resume_entities.Education;
import com.rebuild.backend.model.entities.resume_entities.Experience;
import com.rebuild.backend.model.entities.resume_entities.Header;
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
@RequestMapping(value = "/api/resume/post", method = RequestMethod.POST)
@ResponseStatus(HttpStatus.CREATED)
public class ResumePostController {
    private final ResumeService resumeService;

    @Autowired
    public ResumePostController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping("/header/{res_id}")
    public Header createNewHeader(@PathVariable UUID res_id, @Valid @RequestBody HeaderForm headerForm){
        return resumeService.createNewHeader(res_id, headerForm.name(), headerForm.email(), headerForm.number());
    }

    @PostMapping("/experience/{res_id}")
    public Experience createNewExperience(@PathVariable UUID res_id, @Valid @RequestBody ExperienceForm experienceForm){
        Duration duration = Duration.between(experienceForm.startDate(), experienceForm.endDate());
        return resumeService.createNewExperience(res_id, experienceForm.companyName(), experienceForm.technologies(),
                duration, experienceForm.bullets());
    }

    @PostMapping("/education/{res_id}")
    public Education createNewEducation(@PathVariable UUID res_id, @Valid @RequestBody EducationForm educationForm){
        return resumeService.createNewEducation(res_id, educationForm.schoolName(),
                educationForm.relevantCoursework());
    }
}

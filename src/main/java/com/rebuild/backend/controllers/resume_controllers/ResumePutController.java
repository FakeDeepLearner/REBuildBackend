package com.rebuild.backend.controllers.resume_controllers;

import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.forms.resume_forms.*;
import com.rebuild.backend.model.responses.resume_responses.*;
import com.rebuild.backend.service.resume_services.ResumeModificationService;
import com.rebuild.backend.service.resume_services.ResumeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/resume/put", method = RequestMethod.PUT)
@ResponseStatus(HttpStatus.OK)
public class ResumePutController {

    private final ResumeModificationService modificationService;

    @Autowired
    public ResumePutController(ResumeModificationService modificationService) {
        this.modificationService = modificationService;
    }

    @PutMapping("/header/{resume_id}/")
    public ResumeResponse modifyHeader(@Valid @RequestBody HeaderForm headerForm,
                                       @AuthenticationPrincipal User user,
                                       @PathVariable UUID resume_id){
        return modificationService.changeHeaderInfo(headerForm, resume_id, user);

    }

    @PutMapping("/experience/{resume_id}/{experience_id}")
    public ResumeResponse modifyExperience(@Valid @RequestBody ExperienceForm experienceForm,
                                               @AuthenticationPrincipal User user,
                                               @PathVariable UUID experience_id, @PathVariable UUID resume_id){

        return modificationService.modifyResumeExperience(experienceForm, experience_id,
                resume_id, user);
    }

    @PutMapping("/education/{resume_id}")
    @ResponseStatus(HttpStatus.OK)
    public ResumeResponse modifyEducation(@Valid @RequestBody EducationForm educationForm,
                                             @AuthenticationPrincipal User user,
                                             @PathVariable UUID resume_id){
        return modificationService.changeEducationInfo(educationForm,
                resume_id, user);

    }

    @PutMapping("/experience/{resume_id}/{project_id}")
    public ResumeResponse modifyProject(@Valid @RequestBody ProjectForm projectForm,
                                         @AuthenticationPrincipal User user,
                                         @PathVariable UUID project_id, @PathVariable UUID resume_id){
        return modificationService.modifyResumeProject(projectForm, project_id,
                resume_id, user);
    }
}

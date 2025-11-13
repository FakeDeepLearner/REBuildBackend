package com.rebuild.backend.controllers.resume_controllers;

import com.rebuild.backend.model.entities.resume_entities.Education;
import com.rebuild.backend.model.entities.resume_entities.Experience;
import com.rebuild.backend.model.entities.resume_entities.Header;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.forms.resume_forms.EducationForm;
import com.rebuild.backend.model.forms.resume_forms.ExperienceForm;
import com.rebuild.backend.model.forms.resume_forms.HeaderForm;
import com.rebuild.backend.service.resume_services.ResumeService;
import com.rebuild.backend.utils.database_utils.UserContext;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping("/header/{header_id}")
    public ResponseEntity<Header> modifyHeader(@Valid @RequestBody HeaderForm headerForm,
                               @AuthenticationPrincipal User user, @PathVariable UUID header_id){
        UserContext.set(user.getId());
        try {
            Header changedHeader = resumeService.changeHeaderInfo(headerForm, header_id, user);
            UserContext.clear();
            return ResponseEntity.ok(changedHeader);
        }
        catch (AssertionError e)
        {
            UserContext.clear();
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/experience/{experience_id}")
    public ResponseEntity<Experience> modifyExperience(@Valid @RequestBody ExperienceForm experienceForm,
                                              @AuthenticationPrincipal User user, @PathVariable UUID experience_id){
        UserContext.set(user.getId());
        try {
            Experience changedExperience = resumeService.changeExperienceInfo(experienceForm, experience_id,
                    user);
            UserContext.clear();
            return ResponseEntity.ok(changedExperience);
        }
        catch (AssertionError e) {
            UserContext.clear();
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/education/{education_id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Education> modifyEducation(@Valid @RequestBody EducationForm educationForm,
                                     @AuthenticationPrincipal User user, @PathVariable UUID education_id){
        UserContext.set(user.getId());
        try {
            Education changedEducation = resumeService.changeEducationInfo(educationForm, education_id, user);
            UserContext.clear();
            return ResponseEntity.ok(changedEducation);
        }
        catch (AssertionError e)
        {
            UserContext.clear();
            return ResponseEntity.notFound().build();
        }
    }
}

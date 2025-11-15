package com.rebuild.backend.controllers.resume_controllers;

import com.rebuild.backend.model.entities.resume_entities.Education;
import com.rebuild.backend.model.entities.resume_entities.Experience;
import com.rebuild.backend.model.entities.resume_entities.Header;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.forms.resume_forms.EducationForm;
import com.rebuild.backend.model.forms.resume_forms.ExperienceForm;
import com.rebuild.backend.model.forms.resume_forms.HeaderForm;
import com.rebuild.backend.service.resume_services.ResumeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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

    @PutMapping("/header/{resume_id}/{header_id}")
    public ResponseEntity<Header> modifyHeader(@Valid @RequestBody HeaderForm headerForm,
                                               @AuthenticationPrincipal User user,
                                               @PathVariable UUID header_id, @PathVariable UUID resume_id){
        Header changedHeader = resumeService.changeHeaderInfo(headerForm, header_id, resume_id, user);
        return ResponseEntity.ok(changedHeader);

    }

    @PutMapping("/experience/{resume_id}/{experience_id}")
    public ResponseEntity<Experience> modifyExperience(@Valid @RequestBody ExperienceForm experienceForm,
                                                       @AuthenticationPrincipal User user,
                                                       @PathVariable UUID experience_id, @PathVariable UUID resume_id){

        Experience changedExperience = resumeService.changeExperienceInfo(experienceForm, experience_id,
                resume_id,
                    user);
        return ResponseEntity.ok(changedExperience);


    }

    @PutMapping("/education/{resume_id}/{education_id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Education> modifyEducation(@Valid @RequestBody EducationForm educationForm,
                                                     @AuthenticationPrincipal User user,
                                                     @PathVariable UUID education_id, @PathVariable UUID resume_id){
        Education changedEducation = resumeService.changeEducationInfo(educationForm,
                education_id, resume_id, user);
        return ResponseEntity.ok(changedEducation);

    }
}

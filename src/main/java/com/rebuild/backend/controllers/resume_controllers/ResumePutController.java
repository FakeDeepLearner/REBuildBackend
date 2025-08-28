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

    @PutMapping("/header/{index}/{header_id}")
    @CacheEvict(value = "resume_cache", key = "#user.id.toString()" + "-" + "#index")
    public ResponseEntity<Header> modifyHeader(@Valid @RequestBody HeaderForm headerForm, @PathVariable int index,
                               @AuthenticationPrincipal User user, @PathVariable UUID header_id){
        try {
            Header changedHeader = resumeService.changeHeaderInfo(headerForm, header_id);
            return ResponseEntity.ok(changedHeader);
        }
        catch (AssertionError e)
        {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/experience/{resume_index}/{experience_id}")
    @CacheEvict(value = "resume_cache", key = "#user.id.toString()" + "-" + "#resume_index")
    public ResponseEntity<Experience> modifyExperience(@Valid @RequestBody ExperienceForm experienceForm,
                                              @PathVariable int resume_index,
                                              @AuthenticationPrincipal User user, @PathVariable UUID experience_id){
        try {
            Experience changedExperience = resumeService.changeExperienceInfo(experienceForm, experience_id);
            return ResponseEntity.ok(changedExperience);
        }
        catch (AssertionError e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/education/{index}/{education_id}")
    @CacheEvict(value = "resume_cache", key = "#user.id.toString()" + "-" + "#index")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Education> modifyEducation(@Valid @RequestBody EducationForm educationForm,
                                     @PathVariable int index,
                                     @AuthenticationPrincipal User user, @PathVariable UUID education_id){

        try {
            Education changedEducation = resumeService.changeEducationInfo(educationForm, education_id);
            return ResponseEntity.ok(changedEducation);
        }
        catch (AssertionError e)
        {
            return ResponseEntity.notFound().build();
        }
    }
}

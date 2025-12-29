package com.rebuild.backend.controllers.resume_controllers;

import com.rebuild.backend.model.entities.resume_entities.Education;
import com.rebuild.backend.model.entities.resume_entities.Experience;
import com.rebuild.backend.model.entities.resume_entities.Header;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.forms.resume_forms.EducationForm;
import com.rebuild.backend.model.forms.resume_forms.ExperienceForm;
import com.rebuild.backend.model.forms.resume_forms.FullInformationForm;
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

    @PutMapping("/header/{resume_id}/")
    @CacheEvict(cacheManager = "cacheManager", value = "resume_cache", keyGenerator = "resumeCacheKeyGenerator")
    public Resume modifyHeader(@Valid @RequestBody HeaderForm headerForm,
                                               @AuthenticationPrincipal User user,
                                                @PathVariable UUID resume_id){
        return resumeService.changeHeaderInfo(headerForm, resume_id, user);

    }

    @PutMapping("/experience/{resume_id}/{experience_id}")
    @CacheEvict(cacheManager = "cacheManager", value = "resume_cache", key = "#user.id.toString() + ':' + #resume_id")
    public Resume modifyExperience(@Valid @RequestBody ExperienceForm experienceForm,
                                                       @AuthenticationPrincipal User user,
                                                       @PathVariable UUID experience_id, @PathVariable UUID resume_id){

        return resumeService.changeExperienceInfo(experienceForm, experience_id,
                resume_id, user);
    }

    @PutMapping("/education/{resume_id}")
    @CacheEvict(cacheManager = "cacheManager", value = "resume_cache", keyGenerator = "resumeCacheKeyGenerator")
    @ResponseStatus(HttpStatus.OK)
    public Resume modifyEducation(@Valid @RequestBody EducationForm educationForm,
                                                     @AuthenticationPrincipal User user,
                                                     @PathVariable UUID resume_id){
        return resumeService.changeEducationInfo(educationForm,
                resume_id, user);

    }

    @PutMapping("/{resume_id}")
    @ResponseStatus(HttpStatus.OK)
    @CacheEvict(cacheManager = "cacheManager", value = "resume_cache", keyGenerator = "resumeCacheKeyGenerator")
    public Resume updateFullResume(@Valid @RequestBody FullInformationForm fullInformationForm,
                                   @PathVariable UUID resume_id,
                                   @AuthenticationPrincipal User user) {
        return resumeService.fullUpdate(user, resume_id, fullInformationForm);

    }
}

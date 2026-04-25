package com.rebuild.backend.controllers.resume_controllers;

import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.forms.resume_forms.EducationForm;
import com.rebuild.backend.model.forms.resume_forms.ExperienceForm;
import com.rebuild.backend.model.forms.resume_forms.HeaderForm;
import com.rebuild.backend.model.forms.resume_forms.ProjectForm;
import com.rebuild.backend.model.responses.resume_responses.EducationResponse;
import com.rebuild.backend.model.responses.resume_responses.ExperienceResponse;
import com.rebuild.backend.model.responses.resume_responses.HeaderResponse;
import com.rebuild.backend.model.responses.resume_responses.ProjectResponse;
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
@RequestMapping(value = "/api/resume/post", method = RequestMethod.POST)
@ResponseStatus(HttpStatus.CREATED)
@CacheConfig(cacheManager = "cacheManager", cacheNames = "resume_cache")
public class ResumePostController {
    private final ResumeService resumeService;

    @Autowired
    public ResumePostController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping("/header/{resume_id}")
    @CacheEvict(key = "T(com.rebuild.backend.utils.StringUtil).generateResumeCacheKey(#user, #resume_id)")
    public HeaderResponse createNewHeader(@Valid @RequestBody HeaderForm headerForm, @PathVariable UUID resume_id,
                                          @AuthenticationPrincipal User user){
        return resumeService.changeHeaderInfo(headerForm, resume_id, user);
    }

    @PostMapping("/experience/{resume_id}")
    @CacheEvict(key = "T(com.rebuild.backend.utils.StringUtil).generateResumeCacheKey(#user, #resume_id)")
    public ExperienceResponse createNewExperience(@Valid @RequestBody ExperienceForm experienceForm,
                                                  @PathVariable UUID resume_id,
                                                  @AuthenticationPrincipal User user){

        return resumeService.createNewExperience(user, resume_id, experienceForm);

    }

    @PostMapping("/education/{resume_id}")
    @CacheEvict(key = "T(com.rebuild.backend.utils.StringUtil).generateResumeCacheKey(#user, #resume_id)")
    public EducationResponse createNewEducation(@Valid @RequestBody EducationForm educationForm,
                                                @PathVariable UUID resume_id,
                                                @AuthenticationPrincipal User user ){
        return resumeService.changeEducationInfo(educationForm, resume_id, user);
    }

    @PostMapping("/project/{resume_id}")
    @CacheEvict(key = "T(com.rebuild.backend.utils.StringUtil).generateResumeCacheKey(#user, #resume_id)")
    public ProjectResponse createNewProject(@Valid @RequestBody ProjectForm projectForm,
                                            @PathVariable UUID resume_id,
                                            @AuthenticationPrincipal User user){
        return resumeService.createNewProject(user, resume_id, projectForm);
    }
}

package com.rebuild.backend.controllers.resume_controllers;

import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.responses.resume_responses.EducationResponse;
import com.rebuild.backend.model.responses.resume_responses.ExperienceResponse;
import com.rebuild.backend.model.responses.resume_responses.HeaderResponse;
import com.rebuild.backend.model.responses.resume_responses.ProjectResponse;
import com.rebuild.backend.service.resume_services.ResumePrefillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/resume/prefill")
@CacheConfig(cacheManager = "cacheManager", cacheNames = "resume_cache")
public class ResumePrefillController {

    private final ResumePrefillService prefillService;

    @Autowired
    public ResumePrefillController(ResumePrefillService prefillService) {
        this.prefillService = prefillService;
    }

    @PostMapping("/header/{current_resume_id}/{sample_resume_id}")
    @ResponseStatus(HttpStatus.OK)
    @CacheEvict(key = "T(com.rebuild.backend.utils.StringUtil).generateResumeCacheKey(#user, #current_resume_id)")
    public HeaderResponse prefillHeader(@AuthenticationPrincipal User user,
                                        @PathVariable UUID current_resume_id, @PathVariable UUID sample_resume_id)
    {
        return prefillService.prefillResumeHeader(current_resume_id, sample_resume_id, user);
    }

    @PostMapping("/education/{current_resume_id}/{sample_resume_id}")
    @ResponseStatus(HttpStatus.OK)
    @CacheEvict(key = "T(com.rebuild.backend.utils.StringUtil).generateResumeCacheKey(#user, #current_resume_id)")
    public EducationResponse prefillEducation(@AuthenticationPrincipal User user,
                                              @PathVariable UUID current_resume_id, @PathVariable UUID sample_resume_id)
    {
        return prefillService.prefillResumeEducation(current_resume_id, sample_resume_id, user);
    }

    @PostMapping("/experiences/{current_resume_id}/{sample_resume_id}")
    @ResponseStatus(HttpStatus.OK)
    @CacheEvict(key = "T(com.rebuild.backend.utils.StringUtil).generateResumeCacheKey(#user, #current_resume_id)")
    public List<ExperienceResponse> prefillExperiences(@AuthenticationPrincipal User user,
                                                       @PathVariable UUID current_resume_id, @PathVariable UUID sample_resume_id)
    {
        return prefillService.prefillResumeExperiences(current_resume_id, sample_resume_id, user);
    }

    @PostMapping("/projects/{current_resume_id}/{sample_resume_id}")
    @ResponseStatus(HttpStatus.OK)
    @CacheEvict(key = "T(com.rebuild.backend.utils.StringUtil).generateResumeCacheKey(#user, #current_resume_id)")
    public List<ProjectResponse> prefillProjects(@AuthenticationPrincipal User user,
                                                 @PathVariable UUID current_resume_id, @PathVariable UUID sample_resume_id)
    {
        return prefillService.prefillResumeProjects(current_resume_id, sample_resume_id, user);
    }
}

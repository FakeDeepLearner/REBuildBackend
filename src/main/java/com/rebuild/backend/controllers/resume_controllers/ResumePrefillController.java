package com.rebuild.backend.controllers.resume_controllers;

import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.service.resume_services.ResumePrefillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/resume/prefill")
public class ResumePrefillController {

    private final ResumePrefillService prefillService;

    @Autowired
    public ResumePrefillController(ResumePrefillService prefillService) {
        this.prefillService = prefillService;
    }

    @PostMapping("/header/{current_resume_id}/{sample_resume_id}")
    @ResponseStatus(HttpStatus.OK)
    public Resume prefillHeader(@AuthenticationPrincipal User user,
                                @PathVariable UUID current_resume_id, @PathVariable UUID sample_resume_id)
    {
        return prefillService.prefillResumeHeader(current_resume_id, sample_resume_id, user);
    }

    @PostMapping("/education/{current_resume_id}/{sample_resume_id}")
    @ResponseStatus(HttpStatus.OK)
    public Resume prefillEducation(@AuthenticationPrincipal User user,
                                @PathVariable UUID current_resume_id, @PathVariable UUID sample_resume_id)
    {
        return prefillService.prefillResumeEducation(current_resume_id, sample_resume_id, user);
    }

    @PostMapping("/experiences/{current_resume_id}/{sample_resume_id}")
    @ResponseStatus(HttpStatus.OK)
    public Resume prefillExperiences(@AuthenticationPrincipal User user,
                                @PathVariable UUID current_resume_id, @PathVariable UUID sample_resume_id)
    {
        return prefillService.prefillResumeExperiences(current_resume_id, sample_resume_id, user);
    }

    @PostMapping("/projects/{current_resume_id}/{sample_resume_id}")
    @ResponseStatus(HttpStatus.OK)
    public Resume prefillProjects(@AuthenticationPrincipal User user,
                                @PathVariable UUID current_resume_id, @PathVariable UUID sample_resume_id)
    {
        return prefillService.prefillResumeProjects(current_resume_id, sample_resume_id, user);
    }
}

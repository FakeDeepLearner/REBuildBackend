package com.rebuild.backend.controllers.resume_controllers;

import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.service.ResumeService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class ResumeUtilController {

    private final ResumeService resumeService;

    public ResumeUtilController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PutMapping("/api/resume/change_name/{res_id}")
    @ResponseStatus(HttpStatus.OK)
    public Resume changeResumeName(@PathVariable UUID res_id, @RequestBody String newName) {
        return resumeService.changeName(res_id, newName);
    }

    @PostMapping("/api/resume/copy/{res_id}")
    @ResponseStatus(HttpStatus.OK)
    public Resume copyResume(@PathVariable UUID res_id) {
        return resumeService.copyResume(res_id);
    }
}

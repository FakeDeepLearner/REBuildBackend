package com.rebuild.backend.controllers.resume_controllers;

import com.rebuild.backend.model.entities.resume_entities.ResumeSection;
import com.rebuild.backend.model.forms.resume_forms.SectionForm;
import com.rebuild.backend.service.resume_services.ResumeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/resume/sections")
public class SectionController {

    private final ResumeService resumeService;

    @Autowired
    public SectionController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/add_new/{res_id}")
    public ResumeSection addNewSection(@PathVariable UUID res_id, @Valid @RequestBody SectionForm form){
        return resumeService.createNewSection(res_id, form.title(), form.bullets());
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/delete/{res_id}/{section_id}")
    public void deleteSection(@PathVariable UUID res_id, @PathVariable UUID section_id){
        resumeService.deleteSection(res_id, section_id);
    }
}

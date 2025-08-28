package com.rebuild.backend.controllers.resume_controllers;

import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.forms.resume_forms.SectionForm;
import com.rebuild.backend.service.resume_services.ResumeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/resume/sections")
public class SectionController {

    private final ResumeService resumeService;

    @Autowired
    public SectionController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/add_new/{index}")
    @CacheEvict(value = "resume_cache", key = "#user.id.toString()" + "-" + "#index")
    public Resume addNewSection(@Valid @RequestBody SectionForm form,
                                           @PathVariable int index,
                                           @AuthenticationPrincipal User user,
                                           @RequestParam(required = false) Integer sectionsIndex){
        return resumeService.createNewSection(user, index, form, sectionsIndex);

    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/delete/{resume_index}/{section_index}")
    @CacheEvict(value = "resume_cache", key = "#user.id.toString()" + "-" + "#resume_index")
    public Resume deleteSection(@PathVariable int resume_index, @PathVariable int section_index,
                                @AuthenticationPrincipal User user){
        return resumeService.deleteSection(user, resume_index, section_index);
    }
}

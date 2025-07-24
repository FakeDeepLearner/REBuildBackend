package com.rebuild.backend.controllers.resume_controllers;

import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.resume_entities.ResumeSection;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.forms.resume_forms.SectionForm;
import com.rebuild.backend.model.responses.ResultAndErrorResponse;
import com.rebuild.backend.service.resume_services.ResumeService;
import com.rebuild.backend.utils.OptionalValueAndErrorResult;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.UUID;

@RestController
@RequestMapping("/api/resume/sections")
@SuppressWarnings("OptionalGetWithoutIsPresent")
public class SectionController {

    private final ResumeService resumeService;

    @Autowired
    public SectionController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/add_new/{index}")
    @CacheEvict(value = "resume_cache", key = "#user.id.toString()" + "-" + "#index")
    public ResponseEntity<?> addNewSection(@Valid @RequestBody SectionForm form,
                                           @PathVariable int index,
                                           @AuthenticationPrincipal User user,
                                           @RequestParam(required = false) Integer sectionsIndex){
        OptionalValueAndErrorResult<Resume> createResult =
                resumeService.createNewSection(user, index, form, sectionsIndex);
        switch(createResult.returnedStatus()){
            case OK -> {
                return ResponseEntity.ok(createResult.optionalResult().get());
            }
            case CONFLICT -> {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResultAndErrorResponse<>
                        (createResult.optionalResult().get(),
                                createResult.optionalError().get()));
            }

            case INTERNAL_SERVER_ERROR -> {
                return ResponseEntity.internalServerError().body(new ResultAndErrorResponse<>(
                        createResult.optionalResult().get(),
                        createResult.optionalError().get()));
            }
        }
        return null;
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/delete/{resume_index}/{section_index}")
    @CacheEvict(value = "resume_cache", key = "#user.id.toString()" + "-" + "#resume_index")
    public Resume deleteSection(@PathVariable int resume_index, @PathVariable int section_index,
                                @AuthenticationPrincipal User user){
        return resumeService.deleteSection(user, resume_index, section_index);
    }
}

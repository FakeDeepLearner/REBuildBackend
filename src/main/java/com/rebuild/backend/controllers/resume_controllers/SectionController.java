package com.rebuild.backend.controllers.resume_controllers;

import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.resume_entities.ResumeSection;
import com.rebuild.backend.model.forms.resume_forms.SectionForm;
import com.rebuild.backend.model.responses.ResultAndErrorResponse;
import com.rebuild.backend.service.resume_services.ResumeService;
import com.rebuild.backend.utils.OptionalValueAndErrorResult;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping("/add_new/{res_id}")
    public ResponseEntity<?> addNewSection(@PathVariable UUID res_id, @Valid @RequestBody SectionForm form){
        OptionalValueAndErrorResult<Resume> createResult =
                resumeService.createNewSection(res_id, form);
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
    @DeleteMapping("/delete/{res_id}/{section_id}")
    public Resume deleteSection(@PathVariable UUID res_id, @PathVariable UUID section_id){
        return resumeService.deleteSection(res_id, section_id);
    }
}

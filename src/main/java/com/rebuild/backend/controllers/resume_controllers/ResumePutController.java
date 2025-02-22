package com.rebuild.backend.controllers.resume_controllers;

import com.rebuild.backend.exceptions.ServerError;
import com.rebuild.backend.exceptions.resume_exceptions.ResumeCompanyConstraintException;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.forms.resume_forms.EducationForm;
import com.rebuild.backend.model.forms.resume_forms.ExperienceForm;
import com.rebuild.backend.model.forms.resume_forms.HeaderForm;
import com.rebuild.backend.model.responses.ResultAndErrorResponse;
import com.rebuild.backend.service.resume_services.ResumeService;
import com.rebuild.backend.utils.OptionalValueAndErrorResult;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
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

    @PutMapping("/header/{res_id}")
    public Resume modifyHeader(@PathVariable UUID res_id, @Valid @RequestBody HeaderForm headerForm){
        return resumeService.changeHeaderInfo(res_id, headerForm.firstName(), headerForm.lastName(),
                headerForm.email(), headerForm.number());
    }

    @PutMapping("/experience/{res_id}/{exp_id}")
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public ResponseEntity<?> modifyExperience(@PathVariable UUID res_id, @PathVariable UUID exp_id,
                                                   @Valid @RequestBody ExperienceForm experienceForm){

        OptionalValueAndErrorResult<Resume> updateResult =
                resumeService.changeExperienceInfo(res_id, exp_id, experienceForm);
        switch (updateResult.returnedStatus()){
            case OK -> {
                return ResponseEntity.ok(updateResult.optionalResult().get());
            }

            case NOT_FOUND -> {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).
                        body(new ResultAndErrorResponse<>(updateResult.optionalResult().get(),
                                updateResult.optionalError().get()));
            }

            case CONFLICT -> {
                throw new ResumeCompanyConstraintException(updateResult.optionalError().get());
            }

            case INTERNAL_SERVER_ERROR -> {
                return  ResponseEntity.internalServerError().body(new ResultAndErrorResponse<>(
                        updateResult.optionalResult().get(),
                        updateResult.optionalError().get()));
            }
        }
        return null;
    }

    @PutMapping("/education/{res_id}")
    public Resume modifyEducation(@PathVariable UUID res_id, @Valid @RequestBody EducationForm educationForm){
        return resumeService.changeEducationInfo(res_id, educationForm.schoolName(),
                educationForm.relevantCoursework());
    }
}

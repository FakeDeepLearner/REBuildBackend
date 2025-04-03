package com.rebuild.backend.controllers.resume_controllers;

import com.rebuild.backend.exceptions.profile_exceptions.NoAttributeInProfileException;
import com.rebuild.backend.exceptions.profile_exceptions.NoProfileException;
import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.entities.profile_entities.ProfileEducation;
import com.rebuild.backend.model.entities.profile_entities.ProfileHeader;
import com.rebuild.backend.model.responses.ResultAndErrorResponse;
import com.rebuild.backend.service.resume_services.ResumeService;
import com.rebuild.backend.service.undo_services.UndoService;
import com.rebuild.backend.utils.OptionalValueAndErrorResult;
import com.rebuild.backend.utils.UndoAdder;
import com.rebuild.backend.utils.converters.ObjectConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile/prefill")
@SuppressWarnings("OptionalGetWithoutIsPresent")
public class PrefillFromProfileController {
    private final ObjectConverter objectConverter;

    private final ResumeService resumeService;

    private final UndoAdder undoAdder;

    @Autowired
    public PrefillFromProfileController(ObjectConverter objectConverter,
                                        ResumeService resumeService, UndoAdder undoAdder) {
        this.objectConverter = objectConverter;
        this.resumeService = resumeService;
        this.undoAdder = undoAdder;
    }

    @GetMapping("/header/{resume_id}")
    @ResponseStatus(HttpStatus.OK)
    public Resume prefillHeader(@PathVariable UUID resume_id){
        Resume associatedResume = resumeService.findById(resume_id);
        User resumeUser = associatedResume.getUser();
        if(resumeUser.getProfile() == null){
            throw new NoProfileException("You haven't set your profile yet, this operation can't be completed");
        }
        if(resumeUser.getProfile().getHeader() == null){
            throw new NoAttributeInProfileException("Your profile does not have a header set");
        }
        undoAdder.addUndoResumeState(resume_id, associatedResume);
        ProfileHeader originalHeader = resumeUser.getProfile().getHeader();
        Header transformedHeader = objectConverter.convertToHeader(originalHeader);
        return resumeService.setHeader(associatedResume, transformedHeader);
    }

    @GetMapping("/education/{resume_id}")
    @ResponseStatus(HttpStatus.OK)
    public Resume prefillEducation(@PathVariable UUID resume_id){
        Resume associatedResume = resumeService.findById(resume_id);
        User resumeUser = associatedResume.getUser();
        if(resumeUser.getProfile() == null){
            throw new NoProfileException("You haven't set your profile yet, this operation can't be completed");
        }
        if(resumeUser.getProfile().getEducation() == null){
            throw new NoAttributeInProfileException("Your profile does not have an education set");
        }
        undoAdder.addUndoResumeState(resume_id, associatedResume);
        ProfileEducation originalEducation = resumeUser.getProfile().getEducation();
        Education transformedEducation = objectConverter.convertToEducation(originalEducation);
        return resumeService.setEducation(associatedResume, transformedEducation);
    }

    @GetMapping("/experiences/{resume_id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> prefillExperience(@PathVariable UUID resume_id){
        Resume associatedResume = resumeService.findById(resume_id);
        User resumeUser = associatedResume.getUser();
        if(resumeUser.getProfile() == null){
            throw new NoProfileException("You haven't set your profile yet, this operation can't be completed");
        }
        if(resumeUser.getProfile().getExperienceList() == null){
            throw new NoAttributeInProfileException("Your profile does not have experiences set");
        }
        undoAdder.addUndoResumeState(resume_id, associatedResume);
        List<Experience> convertedExperiences = resumeUser.getProfile().getExperienceList().
                stream().map(objectConverter::convertToExperience).
                toList();
        OptionalValueAndErrorResult<Resume> prefillResult =
        resumeService.setExperiences(associatedResume, convertedExperiences);

        switch(prefillResult.returnedStatus()){
            case OK -> {
                return ResponseEntity.ok(prefillResult.optionalResult().get());
            }
            case CONFLICT -> {
                undoAdder.removeUndoState(resume_id);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResultAndErrorResponse<>
                        (prefillResult.optionalResult().get(),
                                prefillResult.optionalError().get()));
            }

            case INTERNAL_SERVER_ERROR -> {
                undoAdder.removeUndoState(resume_id);
                return ResponseEntity.internalServerError().body(new ResultAndErrorResponse<>(
                        prefillResult.optionalResult().get(),
                        prefillResult.optionalError().get()));
            }
        }
        return null;

    }

    @GetMapping("/sections/{resume_id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> prefillSections(@PathVariable UUID resume_id){
        Resume associatedResume = resumeService.findById(resume_id);
        User resumeUser = associatedResume.getUser();
        if(resumeUser.getProfile() == null){
            throw new NoProfileException("You haven't set your profile yet, this operation can't be completed");
        }
        if(resumeUser.getProfile().getSections() == null){
            throw new NoAttributeInProfileException("Your profile does not have sections set");
        }

        undoAdder.addUndoResumeState(resume_id, associatedResume);
        List<ResumeSection> convertedSections = resumeUser.getProfile().getSections().
                stream().map(objectConverter::convertToSection).
                toList();
        OptionalValueAndErrorResult<Resume> prefillResult =
                resumeService.setSections(associatedResume, convertedSections);

        switch(prefillResult.returnedStatus()){
            case OK -> {
                return ResponseEntity.ok(prefillResult.optionalResult().get());
            }
            case CONFLICT -> {
                undoAdder.removeUndoState(resume_id);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResultAndErrorResponse<>
                        (prefillResult.optionalResult().get(),
                                prefillResult.optionalError().get()));

            }

            case INTERNAL_SERVER_ERROR -> {
                undoAdder.removeUndoState(resume_id);
                return ResponseEntity.internalServerError().body(new ResultAndErrorResponse<>(
                        prefillResult.optionalResult().get(),
                        prefillResult.optionalError().get()));
            }
        }
        return null;
    }
}

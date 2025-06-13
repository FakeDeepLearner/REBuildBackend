package com.rebuild.backend.controllers.resume_controllers;

import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.forms.resume_forms.FullResumeForm;
import com.rebuild.backend.model.responses.ResultAndErrorResponse;
import com.rebuild.backend.service.resume_services.ResumeService;
import com.rebuild.backend.utils.OptionalValueAndErrorResult;
import com.rebuild.backend.utils.UndoAdder;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class FullUpdateController {

    private final ResumeService resumeService;

    private final UndoAdder undoAdder;


    @Autowired
    public FullUpdateController(ResumeService resumeService, UndoAdder undoAdder) {
        this.resumeService = resumeService;
        this.undoAdder = undoAdder;
    }

    @PutMapping("/api/put/{res_id}/{index}")
    @ResponseStatus(HttpStatus.OK)
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @CacheEvict(value = "resume_cache", key = "#user.id.toString()" + "-" + "#index")
    public ResponseEntity<?> updateFullResume(@Valid @RequestBody FullResumeForm fullResumeForm,
                                           @PathVariable UUID res_id, @PathVariable int index,
                                              @AuthenticationPrincipal User user) {

        Resume associatedResume = resumeService.findByUserIndex(user, index);

        undoAdder.addUndoResumeState(res_id, associatedResume);
        OptionalValueAndErrorResult<Resume> fullUpdateResult =
                resumeService.fullUpdate(associatedResume, fullResumeForm);

        switch(fullUpdateResult.returnedStatus()){
            case OK -> {
                return ResponseEntity.ok(fullUpdateResult.optionalResult().get());
            }
            case CONFLICT -> {
                undoAdder.removeUndoState(res_id);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResultAndErrorResponse<>
                        (fullUpdateResult.optionalResult().get(),
                                fullUpdateResult.optionalError().get()));
            }

            case INTERNAL_SERVER_ERROR -> {
                undoAdder.removeUndoState(res_id);
                return ResponseEntity.internalServerError().body(new ResultAndErrorResponse<>(
                        fullUpdateResult.optionalResult().get(),
                        fullUpdateResult.optionalError().get()));
            }
        }
        return null;
    }
}

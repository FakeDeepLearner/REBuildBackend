package com.rebuild.backend.controllers.resume_controllers;

import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.resume_entities.ResumeVersion;
import com.rebuild.backend.model.responses.ResultAndErrorResponse;
import com.rebuild.backend.service.resume_services.ResumeService;
import com.rebuild.backend.utils.OptionalValueAndErrorResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/versions")
public class VersioningController {

    private final ResumeService resumeService;

    @Autowired
    public VersioningController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping("/create_version/{resume_id}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResumeVersion snapshotVersion(@PathVariable UUID resume_id){
        return resumeService.snapshotCurrentData(resume_id);
    }

    @GetMapping("/switch_version/{resume_id}/{version_id}")
    @ResponseStatus(HttpStatus.OK)
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public ResponseEntity<?> switchToVersion(@PathVariable UUID resume_id, @PathVariable UUID version_id){
        OptionalValueAndErrorResult<Resume> switchingResult =
                resumeService.switchToAnotherVersion(resume_id, version_id);

        switch(switchingResult.returnedStatus()){
            case OK -> {
                return ResponseEntity.ok(switchingResult.optionalResult().get());
            }
            case CONFLICT -> {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResultAndErrorResponse<>
                        (switchingResult.optionalResult().get(),
                                switchingResult.optionalError().get()));
            }

            case INTERNAL_SERVER_ERROR -> {
                return ResponseEntity.internalServerError().body(new ResultAndErrorResponse<>(
                        switchingResult.optionalResult().get(),
                        switchingResult.optionalError().get()));
            }
        }
        return null;
    }

    @DeleteMapping("/delete_version/{version_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVersion(@PathVariable UUID version_id){
        resumeService.deleteVersion(version_id);
    }

}

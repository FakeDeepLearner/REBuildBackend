package com.rebuild.backend.controllers.resume_controllers;

import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.responses.ResultAndErrorResponse;
import com.rebuild.backend.service.resume_services.ResumeService;
import com.rebuild.backend.utils.OptionalValueAndErrorResult;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@SuppressWarnings("OptionalGetWithoutIsPresent")
public class ResumeUtilController {

    private final ResumeService resumeService;

    public ResumeUtilController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PutMapping("/api/resume/change_name/{res_id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> changeResumeName(@PathVariable UUID res_id, @RequestBody String newName) {
        OptionalValueAndErrorResult<Resume> changingResult =
                resumeService.changeName(res_id, newName);

        switch(changingResult.returnedStatus()){
            case OK -> {
                return ResponseEntity.ok(changingResult.optionalResult().get());
            }
            case CONFLICT -> {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResultAndErrorResponse<>
                        (changingResult.optionalResult().get(),
                                changingResult.optionalError().get()));
            }

            case INTERNAL_SERVER_ERROR -> {
                return ResponseEntity.internalServerError().body(new ResultAndErrorResponse<>(
                        changingResult.optionalResult().get(),
                        changingResult.optionalError().get()));
            }
        }
        return null;
    }

    @PostMapping("/api/resume/copy/{res_id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> copyResume(@PathVariable UUID res_id, @RequestBody String newName) {
        OptionalValueAndErrorResult<Resume> changingResult =
                resumeService.copyResume(res_id, newName);

        switch(changingResult.returnedStatus()){
            case OK -> {
                return ResponseEntity.ok(changingResult.optionalResult().get());
            }
            case CONFLICT -> {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResultAndErrorResponse<>
                        (changingResult.optionalResult().get(),
                                changingResult.optionalError().get()));
            }

            case INTERNAL_SERVER_ERROR -> {
                return ResponseEntity.internalServerError().body(new ResultAndErrorResponse<>(
                        changingResult.optionalResult().get(),
                        changingResult.optionalError().get()));
            }
        }
        return null;
    }

    @GetMapping("/api/download/{res_id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> downloadResumeAsText(@PathVariable UUID res_id,
                                                       @RequestBody boolean includeMetadata) {
        String resumeMetadata = "";

        Resume downloadingResume = resumeService.findById(res_id);
        if (includeMetadata) {
            resumeMetadata = "METADATA: \n" + "\tTime Created: " + downloadingResume.getCreationTime()
                    + "\n\tLast Modified Time: " + downloadingResume.getLastModifiedTime()
                    + "\n\t Download Time " + LocalDateTime.now() + "\n\n";
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", downloadingResume.getName());
        headers.setContentType(MediaType.TEXT_PLAIN);
        return ResponseEntity.status(200).headers(headers).body(resumeMetadata + downloadingResume);

    }
}

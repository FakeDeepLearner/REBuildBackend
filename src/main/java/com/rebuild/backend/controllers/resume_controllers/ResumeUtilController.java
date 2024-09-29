package com.rebuild.backend.controllers.resume_controllers;

import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.service.ResumeService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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

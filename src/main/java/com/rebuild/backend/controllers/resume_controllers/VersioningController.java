package com.rebuild.backend.controllers.resume_controllers;

import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.resume_entities.ResumeVersion;
import com.rebuild.backend.service.resume_services.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    public Resume switchToVersion(@PathVariable UUID resume_id, @PathVariable UUID version_id){
        return resumeService.switchToAnotherVersion(resume_id, version_id);
    }

    @DeleteMapping("/delete_version/{version_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVersion(@PathVariable UUID version_id){
        resumeService.deleteVersion(version_id);
    }

}

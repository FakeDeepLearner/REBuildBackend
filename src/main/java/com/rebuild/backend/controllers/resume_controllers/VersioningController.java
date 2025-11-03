package com.rebuild.backend.controllers.resume_controllers;

import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.entities.versioning_entities.ResumeVersion;
import com.rebuild.backend.model.forms.resume_forms.VersionCreationForm;
import com.rebuild.backend.model.forms.resume_forms.VersionSwitchPreferencesForm;
import com.rebuild.backend.service.resume_services.ResumeVersioningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/versions")
public class VersioningController {

    private final ResumeVersioningService versioningService;

    @Autowired
    public VersioningController(ResumeVersioningService versioningService) {
        this.versioningService = versioningService;
    }

    @PostMapping("/create_version/{resume_id}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResumeVersion snapshotVersion(@AuthenticationPrincipal User user, @PathVariable UUID resume_id,
                                         @RequestBody VersionCreationForm inclusionForm){
        return versioningService.snapshotCurrentData(user, resume_id, inclusionForm);
    }

    @GetMapping("/switch_version/{resume_id}/{version_id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> switchToVersion(@AuthenticationPrincipal User user,
                                             @PathVariable UUID resume_id, @PathVariable UUID version_id,
                                             @RequestBody VersionSwitchPreferencesForm preferencesForm){

        try {
            Resume switchedResume = versioningService.
                    switchToAnotherVersion(user, resume_id, version_id, preferencesForm);
            return ResponseEntity.ok(switchedResume);
        }
        catch (AssertionError e)
        {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete_version/{resume_id}/{version_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVersion(@AuthenticationPrincipal User user,
                              @PathVariable UUID resume_id, @PathVariable UUID version_id){
        versioningService.deleteVersion(user, resume_id, version_id);
    }

}

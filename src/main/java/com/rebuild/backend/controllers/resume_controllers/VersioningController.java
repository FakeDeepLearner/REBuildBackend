package com.rebuild.backend.controllers.resume_controllers;

import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.entities.versioning_entities.ResumeVersion;
import com.rebuild.backend.model.forms.resume_forms.VersionInclusionForm;
import com.rebuild.backend.model.forms.resume_forms.VersionSwitchPreferencesForm;
import com.rebuild.backend.model.responses.ResultAndErrorResponse;
import com.rebuild.backend.service.resume_services.ResumeService;
import com.rebuild.backend.service.resume_services.ResumeVersioningService;
import com.rebuild.backend.utils.OptionalValueAndErrorResult;
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

    @PostMapping("/create_version/{index}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResumeVersion snapshotVersion(@AuthenticationPrincipal User user, @PathVariable int index,
                                         @RequestBody VersionInclusionForm inclusionForm){
        return versioningService.snapshotCurrentData(user, index, inclusionForm);
    }

    @GetMapping("/switch_version/{resume_index}/{version_index}")
    @ResponseStatus(HttpStatus.OK)
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public ResponseEntity<?> switchToVersion(@AuthenticationPrincipal User user,
                                             @PathVariable int resume_index, @PathVariable int version_index,
                                             @RequestBody VersionSwitchPreferencesForm preferencesForm){
        OptionalValueAndErrorResult<Resume> switchingResult =
                versioningService.switchToAnotherVersion(user, resume_index, version_index, preferencesForm);

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

    @DeleteMapping("/delete_version/{resume_index}/{version_index}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVersion(@AuthenticationPrincipal User user,
                              @PathVariable int resume_index, @PathVariable int version_index){
        versioningService.deleteVersion(user, resume_index, version_index);
    }

}

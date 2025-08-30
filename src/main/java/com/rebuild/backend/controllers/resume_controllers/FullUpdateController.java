package com.rebuild.backend.controllers.resume_controllers;

import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.forms.resume_forms.FullInformationForm;
import com.rebuild.backend.service.resume_services.ResumeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class FullUpdateController {

    private final ResumeService resumeService;


    @Autowired
    public FullUpdateController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PutMapping("/api/put/{index}")
    @ResponseStatus(HttpStatus.OK)
    @CacheEvict(value = "resume_cache", key = "#user.id.toString()" + "-" + "#index")
    public Resume updateFullResume(@Valid @RequestBody FullInformationForm fullInformationForm,
                                            @PathVariable int index,
                                              @AuthenticationPrincipal User user) {
        Resume associatedResume = resumeService.findByUserIndex(user, index);
        return resumeService.fullUpdate(associatedResume, fullInformationForm);

    }
}

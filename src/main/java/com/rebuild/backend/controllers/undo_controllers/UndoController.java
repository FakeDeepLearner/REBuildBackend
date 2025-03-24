package com.rebuild.backend.controllers.undo_controllers;

import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.repository.ProfileRepository;
import com.rebuild.backend.repository.ResumeRepository;
import com.rebuild.backend.service.undo_services.UndoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/undo")
public class UndoController {

    private final ResumeRepository resumeRepository;

    private final ProfileRepository profileRepository;

    private final UndoService undoService;

    @Autowired
    public UndoController(ResumeRepository resumeRepository,
                          ProfileRepository profileRepository,
                          UndoService undoService) {
        this.resumeRepository = resumeRepository;
        this.profileRepository = profileRepository;
        this.undoService = undoService;
    }

    @PostMapping("/resume/{resume_id}")
    public Resume revertResume(@PathVariable UUID resume_id){
        Resume currentState = resumeRepository.findById(resume_id).orElse(null);
        assert currentState != null;
        Resume undoneResume = undoService.revertResume(resume_id, currentState);
        return resumeRepository.save(undoneResume);
    }

    @PostMapping("/profile/{profile_id}")
    public UserProfile revertUserProfile(@PathVariable UUID profile_id){

        UserProfile currentState = profileRepository.findById(profile_id).orElse(null);
        assert currentState != null;
        UserProfile undoneProfile = undoService.revertProfile(profile_id, currentState);
        return profileRepository.save(undoneProfile);
    }
}

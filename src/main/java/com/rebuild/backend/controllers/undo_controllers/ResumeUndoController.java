package com.rebuild.backend.controllers.undo_controllers;

import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.repository.ResumeRepository;
import com.rebuild.backend.service.undo_services.ResumeUndoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/undo/resume")
public class ResumeUndoController {

    private final ResumeRepository resumeRepository;


    private final ResumeUndoService resumeUndoService;

    @Autowired
    public ResumeUndoController(ResumeRepository resumeRepository, ResumeUndoService resumeUndoService) {
        this.resumeRepository = resumeRepository;
        this.resumeUndoService = resumeUndoService;
    }

    @PostMapping("/{resume_id}")
    public Resume revertResume(@PathVariable UUID resume_id){
        Resume currentState = resumeRepository.findById(resume_id).orElse(null);
        assert currentState != null;
        return resumeUndoService.revertResume(resume_id, currentState);
    }
}

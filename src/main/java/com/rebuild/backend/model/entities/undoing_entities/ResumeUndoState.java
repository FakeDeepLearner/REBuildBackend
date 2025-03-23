package com.rebuild.backend.model.entities.undoing_entities;

import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.repository.ResumeRepository;

public non-sealed class ResumeUndoState extends UndoState<Resume>{

    private final ResumeRepository resumeRepository;

    public ResumeUndoState(Resume originalResume, ResumeRepository resumeRepository) {
        super(originalResume);
        this.resumeRepository = resumeRepository;
    }


    @Override
    public Resume undoAction(Resume currentState) {
        currentState.setEducation(originalState.getEducation());
        currentState.setHeader(originalState.getHeader());
        currentState.setExperiences(originalState.getExperiences());
        currentState.setSections(originalState.getSections());
        currentState.setSavedVersions(originalState.getSavedVersions());
        currentState.setName(originalState.getName());
        return resumeRepository.save(currentState);

    }


}

package com.rebuild.backend.model.entities.undoing_entities;

import com.rebuild.backend.model.entities.resume_entities.Resume;


public non-sealed class ResumeUndoState extends UndoState<Resume>{

    public ResumeUndoState(Resume originalResume) {
        super(originalResume);
    }


    @Override
    public Resume returnUndoneState(Resume currentState) {
        currentState.setEducation(originalState.getEducation());
        currentState.setHeader(originalState.getHeader());
        currentState.setExperiences(originalState.getExperiences());
        currentState.setSections(originalState.getSections());
        currentState.setName(originalState.getName());
        return currentState;
    }
}

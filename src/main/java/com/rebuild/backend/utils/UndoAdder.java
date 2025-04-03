package com.rebuild.backend.utils;

import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.undoing_entities.ProfileUndoState;
import com.rebuild.backend.model.entities.undoing_entities.ResumeUndoState;
import com.rebuild.backend.model.entities.undoing_entities.UndoState;
import com.rebuild.backend.service.undo_services.UndoService;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UndoAdder {

    private final UndoService undoService;

    @Autowired
    public UndoAdder(UndoService undoService) {
        this.undoService = undoService;
    }

    public void addUndoResumeState(UUID resume_id, Resume originalState){
        UndoState<Resume> undoState = new ResumeUndoState(originalState);
        undoService.addUndoState(resume_id, undoState);
    }

    public void addProfileUndoState(UUID profile_id, UserProfile originalState){
        UndoState<UserProfile> undoState = new ProfileUndoState(originalState);
        undoService.addUndoState(profile_id, undoState);
    }

    public void removeUndoState(UUID state_id){
        undoService.removeUndoState(state_id);
    }


}

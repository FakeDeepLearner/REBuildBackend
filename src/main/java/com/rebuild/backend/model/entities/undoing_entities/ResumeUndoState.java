package com.rebuild.backend.model.entities.undoing_entities;

import com.rebuild.backend.model.entities.resume_entities.Resume;

public non-sealed class ResumeUndoState extends UndoState<Resume>{


    @Override
    Resume undoAction() {
        return null;
    }


}

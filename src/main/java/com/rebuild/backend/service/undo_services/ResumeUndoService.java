package com.rebuild.backend.service.undo_services;

import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.undoing_entities.ResumeUndoState;
import com.rebuild.backend.repository.ResumeRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ResumeUndoService {

    private final Map<UUID, ResumeUndoState> undoStates = new ConcurrentHashMap<>();


    public void addUndoState(UUID resume_id, ResumeUndoState undoState) {
        undoStates.put(resume_id, undoState);
    }

    public Resume revertResume(UUID id, Resume currentState) {
        ResumeUndoState undoState = undoStates.get(id);
        Resume result = undoState.undoAction(currentState);
        undoStates.remove(id);
        return result;
    }
}

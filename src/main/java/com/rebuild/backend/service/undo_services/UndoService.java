package com.rebuild.backend.service.undo_services;

import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.undoing_entities.ProfileUndoState;
import com.rebuild.backend.model.entities.undoing_entities.ResumeUndoState;
import com.rebuild.backend.model.entities.undoing_entities.UndoState;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UndoService {

    private final Map<UUID, UndoState<?>> undoStates = new ConcurrentHashMap<>();


    public void addUndoState(UUID id, UndoState<?> undoState) {
        undoStates.put(id, undoState);
    }

    public void removeUndoState(UUID id) {
        undoStates.remove(id);
    }

    public Resume revertResume(UUID id, Resume currentState) {
        ResumeUndoState undoState = ((ResumeUndoState) undoStates.get(id));
        Resume result = undoState.returnUndoneState(currentState);
        undoStates.remove(id);
        return result;
    }

    public UserProfile revertProfile(UUID id, UserProfile currentState) {
        ProfileUndoState undoState = ((ProfileUndoState) undoStates.get(id));
        UserProfile result = undoState.returnUndoneState(currentState);
        undoStates.remove(id);
        return result;
    }
}

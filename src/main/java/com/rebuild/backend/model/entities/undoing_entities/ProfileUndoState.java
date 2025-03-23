package com.rebuild.backend.model.entities.undoing_entities;

import com.rebuild.backend.model.entities.profile_entities.UserProfile;

public non-sealed class ProfileUndoState extends UndoState<UserProfile> {
    @Override
    UserProfile undoAction() {
        return null;
    }
}

package com.rebuild.backend.model.entities.undoing_entities;

import com.rebuild.backend.model.entities.profile_entities.UserProfile;


public non-sealed class ProfileUndoState extends UndoState<UserProfile> {


    public ProfileUndoState(UserProfile originalState) {
        super(originalState);
    }

    @Override
    public UserProfile returnUndoneState(UserProfile currentState) {
        currentState.setExperienceList(originalState.getExperienceList());
        currentState.setSections(originalState.getSections());
        currentState.setEducation(originalState.getEducation());
        currentState.setHeader(originalState.getHeader());
        currentState.setForumPageSize(originalState.getForumPageSize());
        return currentState;
    }
}

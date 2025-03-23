package com.rebuild.backend.model.entities.undoing_entities;

public abstract sealed class UndoState<T>
permits ProfileUndoState, ResumeUndoState
{

    T originalState;

    abstract T undoAction();
}

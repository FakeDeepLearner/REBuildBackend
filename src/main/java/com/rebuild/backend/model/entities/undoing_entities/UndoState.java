package com.rebuild.backend.model.entities.undoing_entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public abstract sealed class UndoState<T>
permits ProfileUndoState, ResumeUndoState
{

    protected T originalState;

    /* Transforms the current state back into the original state
        */
    public abstract T returnUndoneState(T currentState);
}

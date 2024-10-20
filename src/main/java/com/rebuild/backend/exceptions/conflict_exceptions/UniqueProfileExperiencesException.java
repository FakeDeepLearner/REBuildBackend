package com.rebuild.backend.exceptions.conflict_exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UniqueProfileExperiencesException extends IllegalStateException {
    public UniqueProfileExperiencesException(String message) {
        super(message);
    }
}

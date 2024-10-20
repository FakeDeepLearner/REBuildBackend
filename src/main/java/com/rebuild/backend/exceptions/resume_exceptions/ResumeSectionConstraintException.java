package com.rebuild.backend.exceptions.resume_exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ResumeSectionConstraintException extends IllegalArgumentException {
    public ResumeSectionConstraintException(String message) {
        super(message);
    }
}

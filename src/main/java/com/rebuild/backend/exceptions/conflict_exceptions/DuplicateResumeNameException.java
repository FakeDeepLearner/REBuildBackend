package com.rebuild.backend.exceptions.conflict_exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateResumeNameException extends IllegalArgumentException {
    public DuplicateResumeNameException(String message) {
        super(message);
    }
}

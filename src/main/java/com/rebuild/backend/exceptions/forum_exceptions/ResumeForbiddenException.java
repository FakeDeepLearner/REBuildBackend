package com.rebuild.backend.exceptions.forum_exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ResumeForbiddenException extends RuntimeException {
    public ResumeForbiddenException(String message) {
        super(message);
    }
}

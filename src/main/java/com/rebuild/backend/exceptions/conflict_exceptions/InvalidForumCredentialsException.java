package com.rebuild.backend.exceptions.conflict_exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class InvalidForumCredentialsException extends IllegalStateException {
    public InvalidForumCredentialsException(String message) {
        super(message);
    }
}

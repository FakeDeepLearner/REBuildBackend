package com.rebuild.backend.exceptions.profile_exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class NoAttributeInProfileException extends UnsupportedOperationException {
    public NoAttributeInProfileException(String message) {
        super(message);
    }
}

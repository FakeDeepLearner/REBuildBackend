package com.rebuild.backend.utils.exceptions;

import org.springframework.http.HttpStatus;

public class UserAuthException extends ApiException {
    public UserAuthException(HttpStatus status,
                             String message) {
        super(status, message);
    }
}

package com.rebuild.backend.model.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;

public class UserAuthException extends ApiException {
    public UserAuthException(HttpStatus status,
                             String message) {
        super(status, message);
    }
}

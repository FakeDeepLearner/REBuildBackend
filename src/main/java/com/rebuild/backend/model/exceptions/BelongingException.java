package com.rebuild.backend.model.exceptions;


import org.springframework.http.HttpStatus;

public class BelongingException extends ApiException {
    public BelongingException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}

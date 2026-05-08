package com.rebuild.backend.utils.exceptions;


import org.springframework.http.HttpStatus;

public class BelongingException extends ApiException {
    public BelongingException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}

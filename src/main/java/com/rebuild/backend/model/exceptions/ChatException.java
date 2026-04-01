package com.rebuild.backend.model.exceptions;

import org.springframework.http.HttpStatus;

public class ChatException extends ApiException{
    public ChatException(HttpStatus status, String message) {
        super(status, message);
    }
}

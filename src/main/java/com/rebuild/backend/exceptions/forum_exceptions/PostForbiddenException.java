package com.rebuild.backend.exceptions.forum_exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class PostForbiddenException extends RuntimeException {
    public PostForbiddenException(String message) {
        super(message);
    }
}

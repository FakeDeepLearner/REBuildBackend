package com.rebuild.backend.utils.exceptions;


import org.springframework.http.HttpStatus;

public class FriendshipException extends ApiException {
    public FriendshipException(HttpStatus status, String message) {
        super(status, message);
    }
}

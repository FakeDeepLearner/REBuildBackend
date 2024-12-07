package com.rebuild.backend.exceptions.jwt_exceptions;

import lombok.Getter;

@Getter
public class JWTTokenExpiredException extends IllegalStateException{
    private final String expiredToken;

    public JWTTokenExpiredException(String message, String expiredToken){
        super(message);
        this.expiredToken = expiredToken;
    }

}

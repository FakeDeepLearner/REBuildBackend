package com.rebuild.backend.exceptions;

public class JWTTokenExpiredException extends IllegalStateException{
    public JWTTokenExpiredException(String message){
        super(message);
    }
}

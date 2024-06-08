package com.rebuild.backend.exceptions.jwt_exceptions;

public class NoJWTTokenException extends IllegalStateException{
    public NoJWTTokenException(String message){
        super(message);
    }
}

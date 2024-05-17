package com.rebuild.backend.exceptions;

public class NoJWTTokenException extends IllegalStateException{
    public NoJWTTokenException(String message){
        super(message);
    }
}

package com.rebuild.backend.exceptions.token_exceptions;

public class ActivationTokenExpiredException extends IllegalStateException{
    public ActivationTokenExpiredException(String message){
        super(message);
    }
}

package com.rebuild.backend.exceptions.token_exceptions;



public class ActivationTokenNotFoundException extends IllegalStateException{
    public ActivationTokenNotFoundException(String message){
        super(message);
    }
}

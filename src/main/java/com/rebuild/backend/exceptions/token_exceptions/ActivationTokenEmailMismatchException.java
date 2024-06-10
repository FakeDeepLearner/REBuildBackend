package com.rebuild.backend.exceptions.token_exceptions;

public class ActivationTokenEmailMismatchException extends IllegalStateException{
    public ActivationTokenEmailMismatchException(String message){
        super(message);
    }
}

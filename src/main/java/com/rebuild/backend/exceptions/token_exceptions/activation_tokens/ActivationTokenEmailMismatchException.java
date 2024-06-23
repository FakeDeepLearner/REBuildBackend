package com.rebuild.backend.exceptions.token_exceptions.activation_tokens;


public non-sealed class ActivationTokenEmailMismatchException extends ActivationTokenException {
    public ActivationTokenEmailMismatchException(String message){
        super(message);
    }
}

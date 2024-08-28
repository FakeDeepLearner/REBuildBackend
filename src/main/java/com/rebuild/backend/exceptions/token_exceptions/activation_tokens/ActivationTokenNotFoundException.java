package com.rebuild.backend.exceptions.token_exceptions.activation_tokens;



public non-sealed class ActivationTokenNotFoundException extends ActivationTokenException {
    public ActivationTokenNotFoundException(String message){
        super(message);
    }
}

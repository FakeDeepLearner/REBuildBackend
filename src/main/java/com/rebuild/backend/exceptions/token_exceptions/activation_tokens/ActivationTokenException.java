package com.rebuild.backend.exceptions.token_exceptions.activation_tokens;

public abstract sealed class ActivationTokenException extends IllegalStateException
permits ActivationTokenExpiredException, ActivationTokenNotFoundException, ActivationTokenEmailMismatchException
{

    public ActivationTokenException(String message){
        super(message);
    }
}

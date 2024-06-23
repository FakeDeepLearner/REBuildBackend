package com.rebuild.backend.exceptions.token_exceptions.activation_tokens;

import com.rebuild.backend.exceptions.token_exceptions.activation_tokens.ActivationTokenException;

public non-sealed class ActivationTokenExpiredException extends ActivationTokenException {

    private final String failedEmailFor;
    public ActivationTokenExpiredException(String message, String failedEmailFor){
        super(message);
        this.failedEmailFor = failedEmailFor;
    }

    public String getFailedEmailFor() {
        return failedEmailFor;
    }
}

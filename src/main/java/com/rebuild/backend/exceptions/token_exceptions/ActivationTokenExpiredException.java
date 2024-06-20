package com.rebuild.backend.exceptions.token_exceptions;

public class ActivationTokenExpiredException extends IllegalStateException{

    private final String failedEmailFor;
    public ActivationTokenExpiredException(String message, String failedEmailFor){
        super(message);
        this.failedEmailFor = failedEmailFor;
    }

    public String getFailedEmailFor() {
        return failedEmailFor;
    }
}

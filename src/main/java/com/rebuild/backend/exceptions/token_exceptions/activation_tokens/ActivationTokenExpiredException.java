package com.rebuild.backend.exceptions.token_exceptions.activation_tokens;

public non-sealed class ActivationTokenExpiredException extends ActivationTokenException {

    private final String failedEmailFor;

    private final boolean remembered;

    private final String enteredPassword;
    public ActivationTokenExpiredException(String message, String failedEmailFor,
                                           boolean remembered,
                                           String enteredPassword){
        super(message);
        this.failedEmailFor = failedEmailFor;
        this.remembered = remembered;
        this.enteredPassword = enteredPassword;
    }

    public String getFailedEmailFor() {
        return failedEmailFor;
    }

    public String getEnteredPassword() {
        return enteredPassword;
    }

    public boolean isRemembered() {
        return remembered;
    }
}

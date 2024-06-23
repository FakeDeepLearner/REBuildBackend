package com.rebuild.backend.exceptions.token_exceptions.reset_tokens;

public non-sealed class ResetTokenExpiredException extends ResetTokenException{
    private final String emailFor;

    public ResetTokenExpiredException(String message, String emailFor){
        super(message);
        this.emailFor = emailFor;
    }

    public String getEmailFor() {
        return emailFor;
    }
}

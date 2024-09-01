package com.rebuild.backend.exceptions.token_exceptions.email_change_tokens;

public non-sealed class EmailTokenNotFoundException extends EmailChangeTokenException{
    public EmailTokenNotFoundException(String message) {
        super(message);
    }
}

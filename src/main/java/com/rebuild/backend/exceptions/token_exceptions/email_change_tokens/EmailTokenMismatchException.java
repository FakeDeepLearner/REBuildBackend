package com.rebuild.backend.exceptions.token_exceptions.email_change_tokens;

public non-sealed class EmailTokenMismatchException extends EmailChangeTokenException {
    public EmailTokenMismatchException(String message) {
        super(message);
    }
}

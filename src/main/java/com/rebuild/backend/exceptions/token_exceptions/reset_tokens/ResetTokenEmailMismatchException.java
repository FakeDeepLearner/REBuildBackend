package com.rebuild.backend.exceptions.token_exceptions.reset_tokens;

public non-sealed class ResetTokenEmailMismatchException extends ResetTokenException{
    public ResetTokenEmailMismatchException(String message) {
        super(message);
    }
}

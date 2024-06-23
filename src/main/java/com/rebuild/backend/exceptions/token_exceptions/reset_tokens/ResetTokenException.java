package com.rebuild.backend.exceptions.token_exceptions.reset_tokens;

public abstract sealed class ResetTokenException extends IllegalStateException
permits ResetTokenExpiredException, ResetTokenNotFoundException, ResetTokenEmailMismatchException{
    public ResetTokenException(String message){
        super(message);
    }
}

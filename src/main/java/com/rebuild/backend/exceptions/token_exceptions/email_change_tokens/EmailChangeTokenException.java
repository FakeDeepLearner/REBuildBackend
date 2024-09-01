package com.rebuild.backend.exceptions.token_exceptions.email_change_tokens;

public abstract sealed class EmailChangeTokenException extends IllegalStateException
permits EmailTokenNotFoundException, EmailTokenExpiredException, EmailTokenMismatchException  {
    public EmailChangeTokenException(String message){
        super(message);
    }
}

package com.rebuild.backend.exceptions.token_exceptions;

public class TokenBlackListedException extends IllegalStateException{
    public TokenBlackListedException(String message){
        super(message);
    }
}

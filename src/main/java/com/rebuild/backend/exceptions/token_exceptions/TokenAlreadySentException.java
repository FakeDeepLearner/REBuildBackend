package com.rebuild.backend.exceptions.token_exceptions;

public class TokenAlreadySentException extends IllegalStateException{
    public TokenAlreadySentException(String message){
        super(message);
    }
}

package com.rebuild.backend.exceptions;

public class JWTCredentialsMismatchException extends IllegalStateException{
    public JWTCredentialsMismatchException(String message){
        super(message);
    }
}

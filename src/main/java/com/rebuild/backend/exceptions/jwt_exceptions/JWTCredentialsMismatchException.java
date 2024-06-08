package com.rebuild.backend.exceptions.jwt_exceptions;

public class JWTCredentialsMismatchException extends IllegalStateException{
    public JWTCredentialsMismatchException(String message){
        super(message);
    }
}

package com.rebuild.backend.exceptions;

public class UsernameAlreadyExistsException extends IllegalArgumentException{
    public UsernameAlreadyExistsException(String message){
        super(message);
    }
}

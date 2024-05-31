package com.rebuild.backend.exceptions;

public class EmailDoesNotExistException extends IllegalArgumentException{

    public EmailDoesNotExistException(String message){
        super(message);
    }
}

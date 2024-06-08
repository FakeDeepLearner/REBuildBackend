package com.rebuild.backend.exceptions.not_found_exceptions;

public class EmailDoesNotExistException extends IllegalArgumentException{

    public EmailDoesNotExistException(String message){
        super(message);
    }
}

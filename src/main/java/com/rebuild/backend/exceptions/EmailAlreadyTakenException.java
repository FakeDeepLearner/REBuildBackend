package com.rebuild.backend.exceptions;

public class EmailAlreadyTakenException extends IllegalArgumentException{

    public EmailAlreadyTakenException(String message){
        super(message);
    }
}

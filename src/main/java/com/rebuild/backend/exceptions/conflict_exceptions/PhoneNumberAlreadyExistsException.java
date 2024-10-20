package com.rebuild.backend.exceptions.conflict_exceptions;

public class PhoneNumberAlreadyExistsException extends IllegalStateException{
    public PhoneNumberAlreadyExistsException(String message){
        super(message);
    }
}

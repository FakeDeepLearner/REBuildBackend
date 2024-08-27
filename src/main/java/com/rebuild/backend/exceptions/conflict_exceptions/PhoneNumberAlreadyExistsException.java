package com.rebuild.backend.exceptions.conflict_exceptions;

public class PhoneNumberAlreadyExistsException extends IllegalArgumentException{
    public PhoneNumberAlreadyExistsException(String message){
        super(message);
    }
}

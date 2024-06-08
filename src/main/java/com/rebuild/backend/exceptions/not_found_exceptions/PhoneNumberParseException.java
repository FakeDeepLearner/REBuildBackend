package com.rebuild.backend.exceptions.not_found_exceptions;

public class PhoneNumberParseException extends IllegalArgumentException{
    public PhoneNumberParseException(String message){
        super(message);
    }
}

package com.rebuild.backend.exceptions;

public class PhoneNumberParseException extends IllegalArgumentException{
    public PhoneNumberParseException(String message){
        super(message);
    }
}

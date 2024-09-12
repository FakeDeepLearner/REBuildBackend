package com.rebuild.backend.exceptions.not_found_exceptions;

public class PhoneNumberMissingException extends IllegalArgumentException{
    public PhoneNumberMissingException(String message){
        super(message);
    }

}

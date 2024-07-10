package com.rebuild.backend.exceptions.rate_limiting_exceptions;

public class UserBlockedException extends RuntimeException{
    public UserBlockedException(String message){
        super(message);
    }

}

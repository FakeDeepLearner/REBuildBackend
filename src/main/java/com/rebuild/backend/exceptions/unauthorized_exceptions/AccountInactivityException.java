package com.rebuild.backend.exceptions.unauthorized_exceptions;

public class AccountInactivityException extends UnsupportedOperationException{
    public AccountInactivityException(String message){
        super(message);
    }
}

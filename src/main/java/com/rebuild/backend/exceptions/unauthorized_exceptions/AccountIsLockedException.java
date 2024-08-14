package com.rebuild.backend.exceptions.unauthorized_exceptions;

public class AccountIsLockedException extends UnsupportedOperationException{
    public AccountIsLockedException(String message){
        super(message);
    }
}

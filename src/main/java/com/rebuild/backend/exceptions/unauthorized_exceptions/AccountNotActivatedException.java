package com.rebuild.backend.exceptions.unauthorized_exceptions;

public class AccountNotActivatedException extends UnsupportedOperationException{
    public AccountNotActivatedException(String message){
        super(message);
    }
}

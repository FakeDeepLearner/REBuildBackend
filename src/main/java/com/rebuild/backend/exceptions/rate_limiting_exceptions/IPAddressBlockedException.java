package com.rebuild.backend.exceptions.rate_limiting_exceptions;

public class IPAddressBlockedException extends RuntimeException{
    public IPAddressBlockedException(String message){
        super(message);
    }
}

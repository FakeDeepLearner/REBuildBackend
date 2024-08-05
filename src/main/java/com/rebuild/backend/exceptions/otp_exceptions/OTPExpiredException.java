package com.rebuild.backend.exceptions.otp_exceptions;

public class OTPExpiredException extends IllegalArgumentException{
    public OTPExpiredException(String message){
        super(message);
    }
}

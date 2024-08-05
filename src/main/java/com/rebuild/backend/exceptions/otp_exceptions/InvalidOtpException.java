package com.rebuild.backend.exceptions.otp_exceptions;

public class InvalidOtpException extends UnsupportedOperationException{
    public InvalidOtpException(String message){
        super(message);
    }
}

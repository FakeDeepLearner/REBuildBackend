package com.rebuild.backend.exceptions.otp_exceptions;

import java.time.Duration;

public class OTPAlreadyGeneratedException extends UnsupportedOperationException{
    public OTPAlreadyGeneratedException(String message){
        super(message);
    }
}

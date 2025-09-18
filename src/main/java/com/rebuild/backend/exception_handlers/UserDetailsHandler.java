package com.rebuild.backend.exception_handlers;

import com.rebuild.backend.service.token_services.OTPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.security.auth.login.AccountExpiredException;

@RestControllerAdvice
public class UserDetailsHandler {

    private final OTPService otpService;

    @Autowired
    public UserDetailsHandler(OTPService otpService) {
        this.otpService = otpService;
    }

    @ExceptionHandler(AccountExpiredException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handleAccountExpiredException(AccountExpiredException e) {

    }
}

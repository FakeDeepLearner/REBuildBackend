package com.rebuild.backend.controllers.exception_handlers;

import com.rebuild.backend.utils.ExceptionBodyBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

//Might be unnecessary because of our filtering implementations already checking for account being locked
// or disabled

@RestControllerAdvice
public class AuthenticationExceptionHandler {

    private final ExceptionBodyBuilder bodyBuilder;

    @Autowired
    public AuthenticationExceptionHandler(ExceptionBodyBuilder bodyBuilder) {
        this.bodyBuilder = bodyBuilder;
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public void handleAuthException(AuthenticationException authenticationException){
        if(authenticationException instanceof LockedException lockedException){

        }
    }
}

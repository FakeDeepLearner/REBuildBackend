package com.rebuild.backend.controllers.exception_handlers;

import com.rebuild.backend.exceptions.token_exceptions.ActivationTokenExpiredException;
import com.rebuild.backend.model.responses.ActivationExpiredResponse;
import com.rebuild.backend.utils.ExceptionBodyBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class AcceptTokenHandler {


    private final ExceptionBodyBuilder bodyBuilder;

    @Autowired
    public AcceptTokenHandler(ExceptionBodyBuilder bodyBuilder) {
        this.bodyBuilder = bodyBuilder;
    }

    @ExceptionHandler(ActivationTokenExpiredException.class)
    @ResponseStatus(HttpStatus.SEE_OTHER)
    public ResponseEntity<ActivationExpiredResponse> handleTokenExpired(ActivationTokenExpiredException e){
        ActivationExpiredResponse expiredResponse = new ActivationExpiredResponse(e.getMessage(), e.getFailedEmailFor());
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/request_new_token");
        return ResponseEntity.status(HttpStatus.SEE_OTHER).headers(headers).body(expiredResponse);

    }
}

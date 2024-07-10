package com.rebuild.backend.controllers.exception_handlers;

import com.rebuild.backend.exceptions.rate_limiting_exceptions.IPAddressBlockedException;
import com.rebuild.backend.utils.ExceptionBodyBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class IPBlockedHandler {
    private final ExceptionBodyBuilder bodyBuilder;

    public IPBlockedHandler(ExceptionBodyBuilder bodyBuilder) {
        this.bodyBuilder = bodyBuilder;
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(IPAddressBlockedException.class)
    public ResponseEntity<Map<String, String>> handleIpBlocked(IPAddressBlockedException e){
        return null;
    }
}

package com.rebuild.backend.exception_handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AssertionHandler {

    @ExceptionHandler({AssertionError.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<String> handleAssertionError(AssertionError assertionError)
    {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(assertionError.getMessage());
    }
}

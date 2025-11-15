package com.rebuild.backend.exception_handlers;

import com.rebuild.backend.model.exceptions.BelongingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class BelongingHandler {

    @ExceptionHandler({BelongingException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<String> handleAssertionError(BelongingException belongingException)
    {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(belongingException.getMessage());
    }
}

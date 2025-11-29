package com.rebuild.backend.exception_handlers;

import com.rebuild.backend.model.exceptions.BelongingException;
import com.rebuild.backend.model.exceptions.PrefillException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PrefillHandler {

    @ExceptionHandler({PrefillException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<String> handlePrefillError(PrefillException prefillException)
    {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(prefillException.getMessage());
    }
}

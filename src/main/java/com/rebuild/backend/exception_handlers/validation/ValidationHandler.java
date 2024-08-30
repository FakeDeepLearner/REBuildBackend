package com.rebuild.backend.exception_handlers.validation;

import com.rebuild.backend.model.responses.ValidationErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class ValidationHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ValidationErrorResponse> handleValidationError(MethodArgumentNotValidException notValidException){
        BindingResult binding = notValidException.getBindingResult();
        List<FieldError> errors = binding.getFieldErrors();
        List<String> failedAttributes = new ArrayList<>();
        List<String> failureMessages = new ArrayList<>();

        for (FieldError error : errors){
            failedAttributes.add(error.getField());
            failureMessages.add(error.getDefaultMessage());
        }

        ValidationErrorResponse responseBody = new ValidationErrorResponse(failedAttributes, failureMessages);
        return ResponseEntity.badRequest().body(responseBody);
    }
}

package com.rebuild.backend.controllers.exception_handlers;

import com.rebuild.backend.exceptions.conflict_exceptions.EmailAlreadyExistsException;
import com.rebuild.backend.exceptions.conflict_exceptions.PhoneNumberAlreadyExistsException;
import com.rebuild.backend.exceptions.resume_exceptions.MaxResumesReachedException;
import com.rebuild.backend.exceptions.resume_exceptions.ResumeCompanyConstraintException;
import com.rebuild.backend.exceptions.conflict_exceptions.UsernameAlreadyExistsException;
import com.rebuild.backend.exceptions.token_exceptions.TokenAlreadySentException;
import com.rebuild.backend.utils.ExceptionBodyBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;


@RestControllerAdvice
public class ConflictStatusHandler {

    private final ExceptionBodyBuilder bodyBuilder;

    @Autowired
    public ConflictStatusHandler(ExceptionBodyBuilder bodyBuilder) {
        this.bodyBuilder = bodyBuilder;
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleEmailTaken(EmailAlreadyExistsException e){
        Map<String, String> body = bodyBuilder.buildBody(e);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleUsernameTaken(UsernameAlreadyExistsException e){
        Map<String, String> body = bodyBuilder.buildBody(e);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(TokenAlreadySentException.class)
    public ResponseEntity<Map<String, String>> handleTokenSent(TokenAlreadySentException e){
        Map<String, String> body = bodyBuilder.buildBody(e);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(ResumeCompanyConstraintException.class)
    public ResponseEntity<Map<String, String>> handleResumeCompanyConstraint(ResumeCompanyConstraintException e){
        Map<String, String> body = bodyBuilder.buildBody(e);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(MaxResumesReachedException.class)
    public ResponseEntity<Map<String, String>> handleMaxResumesReached(MaxResumesReachedException e){
        Map<String, String> body = bodyBuilder.buildBody(e);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(PhoneNumberAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handlePhoneNumberAlreadyExists(PhoneNumberAlreadyExistsException e){
        Map<String, String> body = bodyBuilder.buildBody(e);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

}

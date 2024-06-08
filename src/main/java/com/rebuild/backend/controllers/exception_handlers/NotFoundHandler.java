package com.rebuild.backend.controllers.exception_handlers;

import com.rebuild.backend.exceptions.EmailDoesNotExistException;
import com.rebuild.backend.exceptions.PhoneNumberParseException;
import com.rebuild.backend.exceptions.UserNotFoundException;
import com.rebuild.backend.exceptions.WrongPasswordException;
import org.hibernate.annotations.NotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class NotFoundHandler {

    private final ExceptionBodyBuilder bodyBuilder;

    @Autowired
    public NotFoundHandler(ExceptionBodyBuilder bodyBuilder) {
        this.bodyBuilder = bodyBuilder;
    }


    @ExceptionHandler(EmailDoesNotExistException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ResponseEntity<Map<String, String>> handleEmailDoesNotExist(EmailDoesNotExistException e){
        Map<String, String> body = bodyBuilder.buildBody(e);
        return ResponseEntity.unprocessableEntity().body(body);
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Map<String, String>> handleUserNotFound(UserNotFoundException e){
        Map<String, String> body = bodyBuilder.buildBody(e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(WrongPasswordException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<Map<String, String>> handleWrongPassword(WrongPasswordException e){
        Map<String, String> body = bodyBuilder.buildBody(e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(PhoneNumberParseException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handlePhoneNumberParse(PhoneNumberParseException e){
        Map<String, String> body = bodyBuilder.buildBody(e);
        return ResponseEntity.badRequest().body(body);
    }
}

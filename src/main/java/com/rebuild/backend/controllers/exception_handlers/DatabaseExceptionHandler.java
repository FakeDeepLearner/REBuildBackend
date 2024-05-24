package com.rebuild.backend.controllers.exception_handlers;

import com.rebuild.backend.exceptions.JWTCredentialsMismatchException;
import com.rebuild.backend.exceptions.JWTTokenExpiredException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;


@RestControllerAdvice
public class DatabaseExceptionHandler {

    @ExceptionHandler(JWTTokenExpiredException.class)
    public ResponseEntity<Map<String, String>> handleTokenExpired(JWTTokenExpiredException e){
        HttpHeaders reqHeaders = new HttpHeaders();
        reqHeaders.add("Location", "/login");
        Map<String, String> reqBody = new HashMap<>();
        reqBody.put("message", e.getMessage());
       return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).headers(reqHeaders).body(reqBody);
    }

    @ExceptionHandler(JWTCredentialsMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTokenExpired(JWTCredentialsMismatchException e){
        Map<String, String> reqBody = new HashMap<>();
        reqBody.put("message", e.getMessage());
        reqBody.put("location", "/login");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(reqBody);
    }
}

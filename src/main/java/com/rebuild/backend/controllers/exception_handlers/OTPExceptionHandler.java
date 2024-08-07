package com.rebuild.backend.controllers.exception_handlers;

import com.rebuild.backend.exceptions.otp_exceptions.InvalidOtpException;
import com.rebuild.backend.exceptions.otp_exceptions.OTPAlreadyGeneratedException;
import com.rebuild.backend.exceptions.otp_exceptions.OTPExpiredException;
import com.rebuild.backend.utils.ExceptionBodyBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class OTPExceptionHandler {
    private final ExceptionBodyBuilder bodyBuilder;

    @Autowired
    public OTPExceptionHandler(ExceptionBodyBuilder bodyBuilder) {
        this.bodyBuilder = bodyBuilder;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<Map<String, String>> handleOtpInvalid(InvalidOtpException e){
        Map<String, String> body = bodyBuilder.buildBody(e);
        return ResponseEntity.badRequest().body(body);
    }

    @ResponseStatus(HttpStatus.GONE)
    @ExceptionHandler(OTPExpiredException.class)
    public ResponseEntity<Map<String, String>> handleOtpExpired(OTPExpiredException e){
        Map<String, String> body = bodyBuilder.buildBody(e);
        return ResponseEntity.status(410).body(body);
    }

    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    @ExceptionHandler(OTPAlreadyGeneratedException.class)
    public ResponseEntity<Map<String, String>> handleOtpAlreadyGenerated(OTPAlreadyGeneratedException e){
        Map<String, String> body = bodyBuilder.buildBody(e);
        return ResponseEntity.status(429).body(body);
    }
}

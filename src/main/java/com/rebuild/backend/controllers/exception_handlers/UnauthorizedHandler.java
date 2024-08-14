package com.rebuild.backend.controllers.exception_handlers;

import com.rebuild.backend.exceptions.unauthorized_exceptions.AccountInactivityException;
import com.rebuild.backend.exceptions.unauthorized_exceptions.AccountIsLockedException;
import com.rebuild.backend.exceptions.unauthorized_exceptions.AccountNotActivatedException;
import com.rebuild.backend.utils.ExceptionBodyBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedHandler {

    private final ExceptionBodyBuilder bodyBuilder;

    @ExceptionHandler(AccountInactivityException.class)
    public ResponseEntity<Map<String, String>> handleAccountInactivity(AccountInactivityException e){
        Map<String, String> body = bodyBuilder.buildBody(e);
        return ResponseEntity.status(401).body(body);
    }

    @ExceptionHandler(AccountIsLockedException.class)
    public ResponseEntity<Map<String, String>> handleAccountLocked(AccountIsLockedException e){
        Map<String, String> body = bodyBuilder.buildBody(e);
        return ResponseEntity.status(401).body(body);
    }

    @ExceptionHandler(AccountNotActivatedException.class)
    public ResponseEntity<Map<String, String>> handleAccountNotActivated(AccountNotActivatedException e){
        Map<String, String> body = bodyBuilder.buildBody(e);
        return ResponseEntity.status(401).body(body);
    }


    public UnauthorizedHandler(ExceptionBodyBuilder bodyBuilder) {
        this.bodyBuilder = bodyBuilder;
    }
}

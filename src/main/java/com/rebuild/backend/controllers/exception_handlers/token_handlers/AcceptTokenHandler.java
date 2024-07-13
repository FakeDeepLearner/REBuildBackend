package com.rebuild.backend.controllers.exception_handlers.token_handlers;

import com.rebuild.backend.exceptions.token_exceptions.activation_tokens.ActivationTokenException;
import com.rebuild.backend.exceptions.token_exceptions.activation_tokens.ActivationTokenExpiredException;
import com.rebuild.backend.model.responses.TokenExpiredResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AcceptTokenHandler {

    @ExceptionHandler(ActivationTokenException.class)
    public ResponseEntity<TokenExpiredResponse> handleAcceptException(ActivationTokenException e){
        if (e instanceof ActivationTokenExpiredException expiredException) {
            TokenExpiredResponse expiredResponse =
            new TokenExpiredResponse(expiredException.getMessage(), expiredException.getFailedEmailFor());
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", "/request_new_token_activation");
            return ResponseEntity.status(HttpStatus.SEE_OTHER).headers(headers).body(expiredResponse);
        }
        //TODO: Change these to handle the other 2 exceptions properly
        else{
            return ResponseEntity.notFound().build();
        }


    }
}

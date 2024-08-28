package com.rebuild.backend.controllers.exception_handlers.token_handlers;

import com.rebuild.backend.exceptions.token_exceptions.activation_tokens.ActivationTokenEmailMismatchException;
import com.rebuild.backend.exceptions.token_exceptions.activation_tokens.ActivationTokenException;
import com.rebuild.backend.exceptions.token_exceptions.activation_tokens.ActivationTokenExpiredException;
import com.rebuild.backend.exceptions.token_exceptions.activation_tokens.ActivationTokenNotFoundException;
import com.rebuild.backend.model.responses.TokenExpiredResponse;
import com.rebuild.backend.utils.ExceptionBodyBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AcceptTokenHandler {

    private final ExceptionBodyBuilder bodyBuilder;

    public AcceptTokenHandler(ExceptionBodyBuilder bodyBuilder) {
        this.bodyBuilder = bodyBuilder;
    }

    @ExceptionHandler(ActivationTokenException.class)
    public ResponseEntity<?> handleAcceptException(ActivationTokenException e){
        if (e instanceof ActivationTokenExpiredException expiredException) {
            TokenExpiredResponse expiredResponse =
            new TokenExpiredResponse(expiredException.getMessage(), expiredException.getFailedEmailFor());
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", "/request_new_token_activation");
            return ResponseEntity.status(HttpStatus.SEE_OTHER).headers(headers).body(expiredResponse);
        }
        if(e instanceof ActivationTokenEmailMismatchException emailMismatchException){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(bodyBuilder.buildBody(emailMismatchException));

        }
        //The "else" case represents the not found case
        if(e instanceof ActivationTokenNotFoundException notFoundException){
            return ResponseEntity.status(404).body(bodyBuilder.buildBody(notFoundException));
        }

        //Should never get here.
        return null;
    }
}

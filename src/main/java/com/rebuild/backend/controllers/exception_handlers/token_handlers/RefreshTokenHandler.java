package com.rebuild.backend.controllers.exception_handlers.token_handlers;


import com.rebuild.backend.exceptions.token_exceptions.reset_tokens.ResetTokenException;
import com.rebuild.backend.exceptions.token_exceptions.reset_tokens.ResetTokenExpiredException;
import com.rebuild.backend.model.responses.TokenExpiredResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RefreshTokenHandler {
    @ExceptionHandler(ResetTokenException.class)
    public ResponseEntity<?> handleRefreshException(ResetTokenException resetTokenException){
        if (resetTokenException instanceof ResetTokenExpiredException expiredException) {
            TokenExpiredResponse expiredResponse =
                    new TokenExpiredResponse(expiredException.getMessage(), expiredException.getEmailFor());
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", "/request_new_token_reset");
            return ResponseEntity.status(HttpStatus.SEE_OTHER).headers(headers).body(expiredResponse);
        }
        //TODO: Change these to handle these exceptions properly
        else{
            return ResponseEntity.notFound().build();
        }
    }
}

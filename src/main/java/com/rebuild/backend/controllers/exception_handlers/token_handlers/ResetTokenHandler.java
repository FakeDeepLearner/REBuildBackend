package com.rebuild.backend.controllers.exception_handlers.token_handlers;


import com.rebuild.backend.config.properties.AppUrlBase;
import com.rebuild.backend.exceptions.token_exceptions.reset_tokens.ResetTokenEmailMismatchException;
import com.rebuild.backend.exceptions.token_exceptions.reset_tokens.ResetTokenException;
import com.rebuild.backend.exceptions.token_exceptions.reset_tokens.ResetTokenExpiredException;
import com.rebuild.backend.exceptions.token_exceptions.reset_tokens.ResetTokenNotFoundException;
import com.rebuild.backend.model.responses.TokenExpiredResponse;
import com.rebuild.backend.utils.ExceptionBodyBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ResetTokenHandler {

    private final ExceptionBodyBuilder bodyBuilder;

    private final AppUrlBase urlBase;

    @Autowired
    public ResetTokenHandler(ExceptionBodyBuilder bodyBuilder, AppUrlBase urlBase) {
        this.bodyBuilder = bodyBuilder;
        this.urlBase = urlBase;
    }

    @ExceptionHandler(ResetTokenException.class)
    public ResponseEntity<?> handleRefreshException(ResetTokenException resetTokenException){
        if (resetTokenException instanceof ResetTokenExpiredException expiredException) {
            TokenExpiredResponse expiredResponse =
                    new TokenExpiredResponse(expiredException.getMessage(), expiredException.getEmailFor());
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", urlBase.baseUrl() + "/request_new_token_reset");
            return ResponseEntity.status(HttpStatus.SEE_OTHER).headers(headers).body(expiredResponse);
        }
        if(resetTokenException instanceof ResetTokenEmailMismatchException emailMismatchException){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(bodyBuilder.buildBody(emailMismatchException));
        }

        if(resetTokenException instanceof ResetTokenNotFoundException notFoundException){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(bodyBuilder.buildBody(notFoundException));
        }
        //Should never get here
        return null;
    }
}

package com.rebuild.backend.exception_handlers.token_handlers;

import com.rebuild.backend.config.properties.AppUrlBase;
import com.rebuild.backend.exceptions.token_exceptions.activation_tokens.ActivationTokenEmailMismatchException;
import com.rebuild.backend.exceptions.token_exceptions.activation_tokens.ActivationTokenException;
import com.rebuild.backend.exceptions.token_exceptions.activation_tokens.ActivationTokenExpiredException;
import com.rebuild.backend.exceptions.token_exceptions.activation_tokens.ActivationTokenNotFoundException;
import com.rebuild.backend.model.forms.dtos.ActivationTokenExpiredDTO;
import com.rebuild.backend.utils.ExceptionBodyBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ActivationTokenHandler {

    private final ExceptionBodyBuilder bodyBuilder;

    private final AppUrlBase urlBase;

    @Autowired
    public ActivationTokenHandler(ExceptionBodyBuilder bodyBuilder, AppUrlBase urlBase) {
        this.bodyBuilder = bodyBuilder;
        this.urlBase = urlBase;
    }

    @ExceptionHandler(ActivationTokenException.class)
    public ResponseEntity<?> handleAcceptException(ActivationTokenException e){
        if (e instanceof ActivationTokenExpiredException expiredException) {
            ActivationTokenExpiredDTO expiredDTO = new ActivationTokenExpiredDTO(expiredException.getFailedEmailFor(),
                    expiredException.isRemembered(), expiredException.getEnteredPassword());
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", urlBase.baseUrl() + "/api/request_new_token/activation");
            return ResponseEntity.status(HttpStatus.SEE_OTHER).headers(headers).body(expiredDTO);
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

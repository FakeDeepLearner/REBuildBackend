package com.rebuild.backend.exception_handlers.token_handlers;

import com.rebuild.backend.config.properties.AppUrlBase;
import com.rebuild.backend.exceptions.token_exceptions.email_change_tokens.EmailChangeTokenException;
import com.rebuild.backend.exceptions.token_exceptions.email_change_tokens.EmailTokenExpiredException;
import com.rebuild.backend.exceptions.token_exceptions.email_change_tokens.EmailTokenMismatchException;
import com.rebuild.backend.exceptions.token_exceptions.email_change_tokens.EmailTokenNotFoundException;
import com.rebuild.backend.model.forms.dtos.jwt_tokens_dto.EmailChangeDTO;
import com.rebuild.backend.utils.ExceptionBodyBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmailChangeHandler {

    private final AppUrlBase urlBase;

    private final ExceptionBodyBuilder bodyBuilder;

    @Autowired
    public EmailChangeHandler(AppUrlBase urlBase,
                              ExceptionBodyBuilder bodyBuilder) {
        this.urlBase = urlBase;
        this.bodyBuilder = bodyBuilder;
    }

    @ExceptionHandler(EmailChangeTokenException.class)
    public ResponseEntity<?> handleEmailChangeException(EmailChangeTokenException generalException){
        if(generalException instanceof EmailTokenExpiredException expiredException){
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", urlBase.baseUrl() + "/api/request_new_token/email");
            EmailChangeDTO body = new EmailChangeDTO(expiredException.getOldMail(), expiredException.getNewMail());
            return ResponseEntity.status(HttpStatus.SEE_OTHER).headers(headers).body(body);
        }

        if(generalException instanceof EmailTokenMismatchException emailMismatchException){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(bodyBuilder.buildBody(emailMismatchException));
        }

        if(generalException instanceof EmailTokenNotFoundException notFoundException){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(bodyBuilder.buildBody(notFoundException));
        }
        //Should never get here
        return null;
    }
}

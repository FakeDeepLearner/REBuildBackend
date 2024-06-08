package com.rebuild.backend.controllers.exception_handlers;

import com.rebuild.backend.exceptions.jwt_exceptions.JWTCredentialsMismatchException;
import com.rebuild.backend.exceptions.jwt_exceptions.JWTTokenExpiredException;
import com.rebuild.backend.exceptions.jwt_exceptions.NoJWTTokenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestControllerAdvice
public class AuthenticationExceptionHandler {
    private final ExceptionBodyBuilder bodyBuilder;

    @Autowired
    public AuthenticationExceptionHandler(ExceptionBodyBuilder bodyBuilder) {
        this.bodyBuilder = bodyBuilder;
    }

    @ExceptionHandler(JWTTokenExpiredException.class)
    public ResponseEntity<Map<String, String>> handleTokenExpired(JWTTokenExpiredException e){
        HttpHeaders reqHeaders = new HttpHeaders();
        reqHeaders.add("Authorization", "Bearer " + e.getRefreshToken());
        Map<String, String> reqBody = bodyBuilder.buildBody(e);
        HttpEntity<Map<String, String>> httpEntity = new HttpEntity<>(reqBody, reqHeaders);
        String urlToPost = "/api/refresh_token";

        ParameterizedTypeReference<Map<String, String>>
                typeReference = new ParameterizedTypeReference<Map<String, String>>() {};
        return new RestTemplate().
                exchange(urlToPost,
                        HttpMethod.POST,
                        httpEntity,
                        typeReference);
    }

    @ExceptionHandler(JWTCredentialsMismatchException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<Map<String, String>> handleTokenCredentialsMismatch(JWTCredentialsMismatchException e){
        Map<String, String> reqBody = bodyBuilder.buildBody(e);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("WWW-Authenticate", "Bearer error=\"credentials_mismatch\", " +
                "error_description=\"The credentials you provided and the token do not match\"");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(responseHeaders).body(reqBody);
    }

    @ExceptionHandler(NoJWTTokenException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleNoJwtToken(NoJWTTokenException e){
        Map<String, String> reqBody = bodyBuilder.buildBody(e);
        return ResponseEntity.badRequest().body(reqBody);
    }

}

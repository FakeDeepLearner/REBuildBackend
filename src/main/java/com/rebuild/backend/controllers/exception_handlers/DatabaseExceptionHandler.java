package com.rebuild.backend.controllers.exception_handlers;

import com.rebuild.backend.exceptions.JWTCredentialsMismatchException;
import com.rebuild.backend.exceptions.JWTTokenExpiredException;
import lombok.NonNull;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;


@RestControllerAdvice
public class DatabaseExceptionHandler {

    @ExceptionHandler(JWTTokenExpiredException.class)
    public ResponseEntity<Map<String, String>> handleTokenExpired(JWTTokenExpiredException e){
        HttpHeaders reqHeaders = new HttpHeaders();
        reqHeaders.add("Authorization", "Bearer " + e.getRefreshToken());
        Map<String, String> reqBody = new HashMap<>();
        reqBody.put("message", e.getMessage());
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
        Map<String, String> reqBody = new HashMap<>();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("WWW-Authenticate", "Bearer error=\"credentials_mismatch\", " +
                "error_description=\"The credentials you provided and the token do not match\"");
        reqBody.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(responseHeaders).body(reqBody);
    }
}

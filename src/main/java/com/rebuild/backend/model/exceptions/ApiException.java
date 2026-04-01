package com.rebuild.backend.model.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;

    private final String statusCode;

    private final String message;

    protected ApiException(HttpStatus status, String message)
    {
        this.status = status;
        this.message = message;
        this.statusCode = status.toString();
    }


}

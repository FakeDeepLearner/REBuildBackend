package com.rebuild.backend.model.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    private final String statusCode;


    public ApiException(HttpStatus status, String errorMessage)
    {
        super(errorMessage);
        this.status = status;
        this.statusCode = status.toString();
    }


}

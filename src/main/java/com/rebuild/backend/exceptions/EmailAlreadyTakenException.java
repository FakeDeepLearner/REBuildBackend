package com.rebuild.backend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EmailAlreadyTakenException extends IllegalArgumentException{

    public EmailAlreadyTakenException(String message){
        super(message);
    }
}

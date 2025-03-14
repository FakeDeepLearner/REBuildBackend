package com.rebuild.backend.exceptions.resume_exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ResumeCompanyConstraintException extends IllegalStateException{
    public ResumeCompanyConstraintException(String message){
        super(message);
    }
}

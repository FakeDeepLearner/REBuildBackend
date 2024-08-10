package com.rebuild.backend.exceptions.resume_exceptions;


public class MaxResumesReachedException extends UnsupportedOperationException{
    public MaxResumesReachedException(String message){
        super(message);
    }
}

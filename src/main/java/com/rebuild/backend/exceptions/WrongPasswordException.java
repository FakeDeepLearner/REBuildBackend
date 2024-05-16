package com.rebuild.backend.exceptions;

public class WrongPasswordException extends IllegalArgumentException{
    public WrongPasswordException(String message) {
        super(message);
    }
}

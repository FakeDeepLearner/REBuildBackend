package com.rebuild.backend.exceptions;

public class JWTTokenExpiredException extends IllegalStateException{
    private final String refreshToken;

    public JWTTokenExpiredException(String message, String refreshToken){
        super(message);
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}

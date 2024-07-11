package com.rebuild.backend.exceptions.rate_limiting_exceptions;

import java.time.Duration;

public class UserBlockedException extends RuntimeException{

    private final Duration blockedTimeRemaining;
    public UserBlockedException(String message,
                                Duration blockedTimeRemaining){
        super(message);
        this.blockedTimeRemaining = blockedTimeRemaining;
    }

    public Duration getBlockedTimeRemaining() {
        return blockedTimeRemaining;
    }
}

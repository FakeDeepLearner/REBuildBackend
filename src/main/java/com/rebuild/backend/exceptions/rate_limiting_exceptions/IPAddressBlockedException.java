package com.rebuild.backend.exceptions.rate_limiting_exceptions;

import java.time.Duration;

public class IPAddressBlockedException extends RuntimeException{
    private final Duration blockedTimeRemaining;
    public IPAddressBlockedException(String message, Duration blockedTimeRemaining){
        super(message);
        this.blockedTimeRemaining = blockedTimeRemaining;
    }

    public Duration getBlockedTimeRemaining() {
        return blockedTimeRemaining;
    }
}

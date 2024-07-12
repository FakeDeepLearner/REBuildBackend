package com.rebuild.backend.model.responses;


public record RateLimitingResponse(String errorMessage, int hoursRemaining,
                                   int minutesRemaining, int secondsRemaining) {
}

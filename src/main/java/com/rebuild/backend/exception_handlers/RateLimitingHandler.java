package com.rebuild.backend.exception_handlers;

import com.rebuild.backend.exceptions.rate_limiting_exceptions.IPAddressBlockedException;
import com.rebuild.backend.exceptions.rate_limiting_exceptions.UserBlockedException;
import com.rebuild.backend.model.responses.RateLimitingResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Duration;

@RestControllerAdvice
public class RateLimitingHandler {

    @ExceptionHandler(IPAddressBlockedException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public ResponseEntity<RateLimitingResponse> handleIpLimiting(IPAddressBlockedException exception){
        return getRateLimitingResponseResponseEntity(exception.getBlockedTimeRemaining(), exception.getMessage(), true);
    }

    @ExceptionHandler(UserBlockedException.class)
    @ResponseStatus(HttpStatus.LOCKED)
    public ResponseEntity<RateLimitingResponse> handleUserBlocked(UserBlockedException exception){
        return getRateLimitingResponseResponseEntity(exception.getBlockedTimeRemaining(), exception.getMessage(), false);
    }

    private ResponseEntity<RateLimitingResponse> getRateLimitingResponseResponseEntity(Duration blockedTimeRemaining,
                                                                                       String message, boolean ipBlocked) {
        int[] durationSplit = splitDuration(blockedTimeRemaining);
        HttpStatus responseStatusCode = ipBlocked? HttpStatus.TOO_MANY_REQUESTS  : HttpStatus.LOCKED;
        RateLimitingResponse body = new RateLimitingResponse(message, durationSplit[0],
                durationSplit[1], durationSplit[2]);
        int numSecondsBeforeRetry = durationSplit[0] * 3600 + durationSplit[1] * 60 + durationSplit[2];

        return ResponseEntity.status(responseStatusCode).
                header("Retry-After", String.valueOf(numSecondsBeforeRetry)).
                body(body);
    }

    private int[] splitDuration(Duration duration){
        int[] result = new int[3];

        result[0] = duration.toHoursPart();

        Duration withoutHours = duration.minusHours(result[0]);

        result[1] = duration.toMinutesPart();

        Duration withoutHoursAndMinutes = withoutHours.minusMinutes(result[1]);

        result[2] = withoutHoursAndMinutes.toSecondsPart();

        return result;
    }


}

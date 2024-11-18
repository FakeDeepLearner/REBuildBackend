package com.rebuild.backend.utils;

import org.springframework.http.HttpStatus;

import java.util.Optional;

/*
* A generic type that is used in signaling a result that can also come with an error message,
* both of which are optional
* */
public record OptionalValueAndErrorResult<T>(Optional<T> optionalResult,
                                             Optional<String> optionalError,
                                             HttpStatus returnedStatus) {
    public static <T> OptionalValueAndErrorResult<T> empty() {
        return new OptionalValueAndErrorResult<>(Optional.empty(),
                Optional.empty(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static <T> OptionalValueAndErrorResult<T> of(T value, HttpStatus returnedStatus){
        return new OptionalValueAndErrorResult<>(Optional.of(value), Optional.empty(),
                returnedStatus);
    }

    public static <T> OptionalValueAndErrorResult<T> of(T value, String error, HttpStatus returnedStatus){
        return new OptionalValueAndErrorResult<>(Optional.of(value), Optional.of(error), returnedStatus);
    }

    public static <T> OptionalValueAndErrorResult<T> of(String error, HttpStatus returnedStatus){
        return new OptionalValueAndErrorResult<>(Optional.empty(), Optional.of(error), returnedStatus);
    }
}

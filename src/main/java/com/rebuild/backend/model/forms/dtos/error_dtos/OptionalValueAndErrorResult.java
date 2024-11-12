package com.rebuild.backend.model.forms.dtos.error_dtos;

import com.rebuild.backend.model.entities.resume_entities.Resume;

import java.util.Optional;

/*
* A generic type that is used in signaling a result that can also come with an error message,
* both of which are optional
* */
public record OptionalValueAndErrorResult<T>(Optional<T> optionalResult, Optional<String> optionalError) {
    public static <T> OptionalValueAndErrorResult<T> empty(){
        return new OptionalValueAndErrorResult<>(Optional.empty(), Optional.empty());
    }

    public static <T> OptionalValueAndErrorResult<T> of(T value){
        return new OptionalValueAndErrorResult<>(Optional.of(value), Optional.empty());
    }

    public static <T> OptionalValueAndErrorResult<T> of(T value, String error){
        return new OptionalValueAndErrorResult<>(Optional.of(value), Optional.of(error));
    }

    public static <T> OptionalValueAndErrorResult<T> of(String error){
        return new OptionalValueAndErrorResult<>(Optional.empty(), Optional.of(error));
    }
}

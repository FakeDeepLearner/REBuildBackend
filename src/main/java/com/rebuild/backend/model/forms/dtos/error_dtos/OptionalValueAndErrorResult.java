package com.rebuild.backend.model.forms.dtos.error_dtos;

import com.rebuild.backend.model.entities.users.User;

import java.util.Optional;

/*
* A generic type that is used in signaling a result that can also come with an error message, both of which are optional
* */
public record OptionalValueAndErrorResult<T>(Optional<T> optionalResult, Optional<String> optionalError) {
}

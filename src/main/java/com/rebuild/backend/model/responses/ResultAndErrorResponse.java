package com.rebuild.backend.model.responses;

public record ResultAndErrorResponse<T>(T body, String error) {
}

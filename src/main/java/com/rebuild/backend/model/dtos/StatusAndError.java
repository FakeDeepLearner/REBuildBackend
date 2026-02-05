package com.rebuild.backend.model.dtos;

import org.springframework.http.HttpStatus;

public record StatusAndError(HttpStatus status, String message) {
}

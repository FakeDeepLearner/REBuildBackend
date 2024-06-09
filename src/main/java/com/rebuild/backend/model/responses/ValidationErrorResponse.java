package com.rebuild.backend.model.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ValidationErrorResponse(
        @JsonProperty("fields") List<String> failedFields,
        @JsonProperty("messages") List<String> failureMessages) {
}

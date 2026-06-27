package com.rebuild.backend.model.dtos;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ClerkPhoneNumber(String id, String phoneNumber) {
}

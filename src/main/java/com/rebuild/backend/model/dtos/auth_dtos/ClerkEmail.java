package com.rebuild.backend.model.dtos.auth_dtos;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ClerkEmail(String id, String emailAddress) {
}

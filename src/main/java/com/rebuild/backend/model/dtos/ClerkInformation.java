package com.rebuild.backend.model.dtos;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.PropertyNamingStrategy;
import tools.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ClerkInformation(
        String id,
        String imageUrl,
        Boolean hasImage,
        String username,
        String primaryEmailAddressId,
        List<ClerkEmail> emailAddresses) {
}

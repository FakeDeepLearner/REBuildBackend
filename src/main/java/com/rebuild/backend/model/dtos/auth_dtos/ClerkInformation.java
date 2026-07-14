package com.rebuild.backend.model.dtos.auth_dtos;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ClerkInformation(
        String id,
        String name,
        String imageUrl,
        Boolean hasImage,
        String username,
        String primaryEmailAddressId,
        List<ClerkEmail> emailAddresses,
        String primaryPhoneNumberId,
        List<ClerkPhoneNumber> phoneNumbers) {
}

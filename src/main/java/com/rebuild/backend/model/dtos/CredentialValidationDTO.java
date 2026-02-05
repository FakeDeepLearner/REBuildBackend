package com.rebuild.backend.model.dtos;

import com.rebuild.backend.model.entities.user_entities.User;

public record CredentialValidationDTO(boolean canLogin, User foundUser, boolean enrolledInMFA) {
}

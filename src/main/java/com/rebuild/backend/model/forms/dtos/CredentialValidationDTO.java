package com.rebuild.backend.model.forms.dtos;

public record CredentialValidationDTO(boolean canLogin, String userEmail, String userChannel) {
}

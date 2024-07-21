package com.rebuild.backend.model.responses;

public record AccountActivationResponse(String email, String accessToken, String refreshToken) {
}

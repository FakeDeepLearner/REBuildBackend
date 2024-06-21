package com.rebuild.backend.model.responses;

public record PasswordResetResponse(String oldPassword, String newPassword) {
}

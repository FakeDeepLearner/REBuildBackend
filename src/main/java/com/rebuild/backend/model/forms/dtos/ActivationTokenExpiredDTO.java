package com.rebuild.backend.model.forms.dtos;

public record ActivationTokenExpiredDTO(String email, boolean remembered, String password) {
}

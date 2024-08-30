package com.rebuild.backend.model.forms.dto_forms;

public record ActivationTokenExpiredDTO(String email, boolean remembered, String password) {
}

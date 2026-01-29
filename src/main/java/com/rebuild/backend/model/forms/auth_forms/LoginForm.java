package com.rebuild.backend.model.forms.auth_forms;

public record LoginForm(String emailOrPhone,
                        String password) {
}

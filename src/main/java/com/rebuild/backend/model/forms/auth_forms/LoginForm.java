package com.rebuild.backend.model.forms.auth_forms;

import jakarta.validation.constraints.Email;

public record LoginForm(String emailOrPhone,
                        String password) {
}

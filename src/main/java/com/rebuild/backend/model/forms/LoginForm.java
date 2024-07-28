package com.rebuild.backend.model.forms;

import jakarta.validation.constraints.Email;

public record LoginForm(@Email
                        String email,
                        String password,
                        boolean remember) {
}

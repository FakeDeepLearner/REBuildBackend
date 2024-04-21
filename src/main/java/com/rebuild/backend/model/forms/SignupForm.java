package com.rebuild.backend.model.forms;

import jakarta.validation.constraints.Email;

public record SignupForm(String username,
                         String password,
                         @Email String email) {
}

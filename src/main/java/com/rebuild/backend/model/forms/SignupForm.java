package com.rebuild.backend.model.forms;

import com.rebuild.backend.model.constraints.SignUpPasswordConstraint;
import jakarta.validation.constraints.Email;

public record SignupForm(String username,
                         @SignUpPasswordConstraint
                         String password,
                         @Email String email) {
}

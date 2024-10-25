package com.rebuild.backend.model.forms.forum_forms;

import com.rebuild.backend.model.constraints.password.constraints_and_validators.PasswordStrengthConstraint;
import jakarta.validation.constraints.NotBlank;

public record ForumSignupForm(@NotBlank(message = "Username is required")
                              String username,

                              @PasswordStrengthConstraint
                              @NotBlank(message = "Password is required")
                              String password) {
}

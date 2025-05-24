package com.rebuild.backend.model.forms.auth_forms;

import com.rebuild.backend.model.constraints.password.constraints_and_validators.NoEmailInPasswordConstraint;
import com.rebuild.backend.model.constraints.password.constraints_and_validators.PasswordStrengthConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@NoEmailInPasswordConstraint
public record SignupForm(
                         @Email(message = "Must be a valid email")
                         @NotBlank(message = "Email is required")
                         String email,

                         @PasswordStrengthConstraint
                         @NotBlank(message = "Password is required")
                         String password,

                         @NotBlank(message = "Forum Username is required")
                         String forumUsername,

                         String phoneNumber,
                         boolean remember) {
}

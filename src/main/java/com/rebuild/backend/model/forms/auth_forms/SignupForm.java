package com.rebuild.backend.model.forms.auth_forms;

import com.rebuild.backend.model.constraints.password.constraints_and_validators.NoEmailInPasswordConstraint;
import com.rebuild.backend.model.constraints.password.constraints_and_validators.PasswordStrengthConstraint;
import com.rebuild.backend.model.entities.resume_entities.PhoneNumber;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@NoEmailInPasswordConstraint
public record SignupForm(
                         @Email(message = "Must be a valid email")
                         @NotBlank(message = "Email is required")
                         String email,

                         @PasswordStrengthConstraint
                         @NotBlank(message = "Password is required")
                         String password,

                         PhoneNumber phoneNumber,
                         boolean remember) {
}

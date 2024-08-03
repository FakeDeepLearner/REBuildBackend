package com.rebuild.backend.model.forms;

import com.rebuild.backend.model.constraints.password.constraints_and_validators.NoEmailInPasswordConstraint;
import com.rebuild.backend.model.constraints.password.constraints_and_validators.PasswordStrengthConstraint;
import jakarta.validation.constraints.Email;

@NoEmailInPasswordConstraint
public record SignupForm(
                         @Email(message = "Must be a valid email") String email,

                         @PasswordStrengthConstraint
                         String password,


                         boolean remember) {
}

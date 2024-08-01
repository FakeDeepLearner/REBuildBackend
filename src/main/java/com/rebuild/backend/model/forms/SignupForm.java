package com.rebuild.backend.model.forms;

import com.rebuild.backend.model.constraints.password.NoEmailInPasswordConstraint;
import com.rebuild.backend.model.constraints.password.PasswordSizeAndPatternConstraint;
import com.rebuild.backend.model.constraints.password.PasswordStrengthConstraint;
import jakarta.validation.constraints.Email;

@NoEmailInPasswordConstraint
public record SignupForm(
                         @Email(message = "Must be a valid email") String email,

                         @PasswordSizeAndPatternConstraint
                         @PasswordStrengthConstraint
                         String password,


                         boolean remember) {
}

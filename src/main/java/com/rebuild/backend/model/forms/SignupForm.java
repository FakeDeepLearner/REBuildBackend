package com.rebuild.backend.model.forms;

import com.rebuild.backend.model.constraints.password.NoEmailInPasswordConstraint;
import com.rebuild.backend.model.constraints.password.PasswordSizeAndPatternConstraint;
import jakarta.validation.constraints.Email;

@NoEmailInPasswordConstraint
public record SignupForm(
                         @Email(message = "Must be a valid email") String email,

                         @PasswordSizeAndPatternConstraint
                         String password,


                         boolean remember) {
}

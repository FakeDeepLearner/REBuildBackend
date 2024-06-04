package com.rebuild.backend.model.forms;

import com.rebuild.backend.model.constraints.password.NoUsernameInPasswordConstraint;
import com.rebuild.backend.model.constraints.password.PasswordSizeAndPatternConstraint;
import com.rebuild.backend.model.constraints.username.UsernameLengthConstraint;
import jakarta.validation.constraints.Email;

@NoUsernameInPasswordConstraint
public record SignupForm(@UsernameLengthConstraint
                         String username,
                         @PasswordSizeAndPatternConstraint
                         String password,
                         @Email String email) {
}

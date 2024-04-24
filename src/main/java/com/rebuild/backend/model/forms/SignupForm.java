package com.rebuild.backend.model.forms;

import com.rebuild.backend.model.constraints.password.SignUpPasswordConstraint;
import com.rebuild.backend.model.constraints.username.UsernameLengthConstraint;
import jakarta.validation.constraints.Email;

public record SignupForm(@UsernameLengthConstraint
                         String username,
                         @SignUpPasswordConstraint
                         String password,
                         @Email String email) {
}

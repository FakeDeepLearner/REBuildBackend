package com.rebuild.backend.model.forms;

import com.rebuild.backend.model.constraints.NoUsernameInPasswordConstraint;
import com.rebuild.backend.model.constraints.SignUpPasswordConstraint;
import jakarta.validation.constraints.Email;

public record SignupForm(String username,
                         //TODO: Combine these 2 constraints into one.
                         @SignUpPasswordConstraint
                         @NoUsernameInPasswordConstraint
                         String password,
                         @Email String email) {
}

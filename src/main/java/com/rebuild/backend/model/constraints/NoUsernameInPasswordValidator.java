package com.rebuild.backend.model.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NoUsernameInPasswordValidator implements ConstraintValidator<NoUsernameInPasswordConstraint, String> {


    @Override
    public boolean isValid(String password, ConstraintValidatorContext constraintValidatorContext) {
        //TODO: Replace with actual logic to get the username once security configs are in place
        String obtainedUsername = "example";

        return !password.contains(obtainedUsername);
    }
}

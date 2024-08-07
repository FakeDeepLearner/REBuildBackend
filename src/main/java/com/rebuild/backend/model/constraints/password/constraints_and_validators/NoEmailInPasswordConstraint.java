package com.rebuild.backend.model.constraints.password.constraints_and_validators;

import jakarta.validation.Constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NoUsernameInPasswordValidator.class)
public @interface NoEmailInPasswordConstraint {
    String message() default "The password may not contain your username.";
}

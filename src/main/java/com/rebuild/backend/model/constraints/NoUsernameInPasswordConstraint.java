package com.rebuild.backend.model.constraints;

import jakarta.validation.Constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NoUsernameInPasswordValidator.class)
public @interface NoUsernameInPasswordConstraint {
    String message() default "The password may not contain your username.";
}

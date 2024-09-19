package com.rebuild.backend.model.constraints.resume_and_profile;

import jakarta.validation.Constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DateConsistencyValidator.class)
public @interface EndDateAfterStartDateConstraint {
    String message() default "The end time must be after the start time";
}

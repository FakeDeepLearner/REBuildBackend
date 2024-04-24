package com.rebuild.backend.model.constraints.username;

import jakarta.validation.constraints.Email;
import org.hibernate.validator.constraints.CompositionType;
import org.hibernate.validator.constraints.ConstraintComposition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Email
@UsernameLengthConstraint
@ConstraintComposition(CompositionType.OR)
public @interface LoginFieldConstraint {

    String message() default "This field must either be an email address " +
            "or a nonempty username no longer than 30 characters.";
}

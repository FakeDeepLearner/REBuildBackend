package com.rebuild.backend.model.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Size(min = 8, max = 30)
@Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!]).*$")
@Constraint(validatedBy = {})
public @interface SignUpPasswordConstraint {

    String message() default "The password must contain at least one uppercase letter, at least one lowercase letter," +
            "at least one number and at least one of @, #, $, %, ^, &, +, = or !";
}

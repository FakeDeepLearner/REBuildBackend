package com.rebuild.backend.model.constraints.password;

import com.rebuild.backend.model.constraints.password.NoUsernameInPasswordConstraint;
import com.rebuild.backend.model.constraints.password.PasswordSizeAndPatternConstraint;
import jakarta.validation.Constraint;
import org.hibernate.validator.constraints.CompositionType;
import org.hibernate.validator.constraints.ConstraintComposition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@ConstraintComposition(CompositionType.AND)
@NoUsernameInPasswordConstraint
@PasswordSizeAndPatternConstraint
public @interface SignUpPasswordConstraint {
    String message() default "Invalid Password";
}

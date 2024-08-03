package com.rebuild.backend.model.constraints.password.constraints_and_validators;

import com.rebuild.backend.model.constraints.password.constraints_and_validators.NoEmailInPasswordConstraint;
import com.rebuild.backend.model.forms.SignupForm;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class NoUsernameInPasswordValidator implements ConstraintValidator<NoEmailInPasswordConstraint, SignupForm> {


    @Override
    public boolean isValid(SignupForm form, ConstraintValidatorContext constraintValidatorContext) {
        String obtainedPassword = form.password();

        return !obtainedPassword.contains(form.email());
    }
}

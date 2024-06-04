package com.rebuild.backend.model.constraints.email;

import com.rebuild.backend.model.forms.EmailChangeForm;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EmailMatchValidator implements ConstraintValidator<EmailMatchConstraint, EmailChangeForm> {
    @Override
    public boolean isValid(EmailChangeForm emailChangeForm, ConstraintValidatorContext constraintValidatorContext) {
        return emailChangeForm.newEmail().equals(emailChangeForm.newEmailConfirmation());
    }
}

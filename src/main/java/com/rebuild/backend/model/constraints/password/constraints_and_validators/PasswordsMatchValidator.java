package com.rebuild.backend.model.constraints.password.constraints_and_validators;

import com.rebuild.backend.model.forms.auth_forms.PasswordResetForm;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordsMatchValidator implements ConstraintValidator<PasswordsMatchConstraint, PasswordResetForm> {
    @Override
    public boolean isValid(PasswordResetForm resetForm, ConstraintValidatorContext constraintValidatorContext) {
        return resetForm.newPassword().equals(resetForm.confirmNewPassword());
    }
}

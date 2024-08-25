package com.rebuild.backend.model.forms.auth_forms;

import com.rebuild.backend.model.constraints.password.constraints_and_validators.PasswordStrengthConstraint;
import com.rebuild.backend.model.constraints.password.constraints_and_validators.PasswordsMatchConstraint;

@PasswordsMatchConstraint
public record PasswordResetForm(
                                @PasswordStrengthConstraint
                                String newPassword,
                                String confirmNewPassword) {
}

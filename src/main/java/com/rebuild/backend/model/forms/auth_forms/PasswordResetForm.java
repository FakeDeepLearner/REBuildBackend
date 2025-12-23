package com.rebuild.backend.model.forms.auth_forms;

import com.rebuild.backend.utils.password_utils.PasswordStrengthConstraint;

public record PasswordResetForm(
                                @PasswordStrengthConstraint
                                String newPassword,
                                String confirmNewPassword,
                                String enteredOTP) {
}

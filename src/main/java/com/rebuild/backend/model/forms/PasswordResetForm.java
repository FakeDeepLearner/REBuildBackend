package com.rebuild.backend.model.forms;

import com.rebuild.backend.model.constraints.password.PasswordSizeAndPatternConstraint;
import com.rebuild.backend.model.constraints.password.PasswordsMatchConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@PasswordsMatchConstraint
public record PasswordResetForm(
                                @PasswordSizeAndPatternConstraint
                                String newPassword,
                                @PasswordSizeAndPatternConstraint
                                String confirmNewPassword) {
}

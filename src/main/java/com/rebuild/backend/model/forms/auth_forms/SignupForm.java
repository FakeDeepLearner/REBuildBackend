package com.rebuild.backend.model.forms.auth_forms;

import com.rebuild.backend.utils.password_utils.PasswordStrengthConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignupForm(
                         @Email(message = "Must be a valid email")
                         @NotBlank(message = "Email is required")
                         String email,

                         @PasswordStrengthConstraint
                         @NotBlank(message = "Password is required")
                         String password,

                         @PasswordStrengthConstraint
                         @NotBlank(message = "Repeated password is required")
                         String repeatedPassword,

                         @NotBlank(message = "Timezone is required")
                         String timezoneAsString,

                         String forumUsername,

                         String phoneNumber,

                         String otpChannel,
                         boolean remember) {
}

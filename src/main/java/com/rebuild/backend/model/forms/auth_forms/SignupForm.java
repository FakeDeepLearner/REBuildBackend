package com.rebuild.backend.model.forms.auth_forms;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignupForm(
                         @Email(message = "Must be a valid email")
                         @NotBlank(message = "Email is required")
                         String email,

                         @NotBlank(message = "Password is required")
                         String password,


                         @NotBlank(message = "Repeated password is required")
                         String repeatedPassword,

                         String forumUsername,

                         String phoneNumber,

                         String otpChannel,
                         boolean forcePassword,
                         boolean remember) {
}

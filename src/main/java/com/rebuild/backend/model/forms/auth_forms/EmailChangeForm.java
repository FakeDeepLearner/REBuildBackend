package com.rebuild.backend.model.forms.auth_forms;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailChangeForm(@NotBlank(message = "The email may not be blank")
                              @Email(message = "Must be an email address")
                              String newEmail,
                              @NotBlank(message = "The email confirmation not be blank")
                              @Email(message = "Must be an email address")
                              String newEmailConfirmation,
                              String enteredOTP) {
}

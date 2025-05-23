package com.rebuild.backend.model.forms.auth_forms;

import com.rebuild.backend.model.constraints.email.EmailMatchConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@EmailMatchConstraint
public record EmailChangeForm(@NotBlank(message = "The email may not be blank")
                              @Email(message = "Must be an email address")
                              String newEmail,
                              @NotBlank(message = "The email confirmation not be blank")
                              @Email(message = "Must be an email address")
                              String newEmailConfirmation,
                              String enteredOTP) {
}

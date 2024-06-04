package com.rebuild.backend.model.forms;

import com.rebuild.backend.model.constraints.email.EmailMatchConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@EmailMatchConstraint
public record EmailChangeForm(@NotBlank @Email String newEmail,
                             @NotBlank @Email String newEmailConfirmation) {
}

package com.rebuild.backend.model.forms;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasswordResetForm(@NotBlank(message = "May not be blank")
                                @Email(message = "Must be a valid email")
                                String enteredEmail) {
}

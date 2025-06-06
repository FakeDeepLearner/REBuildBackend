package com.rebuild.backend.model.forms.resume_forms;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record HeaderForm(@NotBlank(message = "First name may not be blank")
                         String firstName,
                         @NotBlank(message = "Last name may not be blank")
                         String lastName,
                         @NotBlank(message = "Email may not be blank")
                         @Email(message = "Must be a valid email")
                         String email,
                         String number) {
}

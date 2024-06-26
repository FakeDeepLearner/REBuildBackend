package com.rebuild.backend.model.forms.resume_forms;

import com.rebuild.backend.model.entities.resume_entities.PhoneNumber;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record HeaderForm(@NotBlank(message = "Name may not be blank")
                         String name,
                         @NotBlank(message = "Email may not be blank")
                         @Email(message = "Must be a valid email")
                         String email,
                         PhoneNumber number) {
}

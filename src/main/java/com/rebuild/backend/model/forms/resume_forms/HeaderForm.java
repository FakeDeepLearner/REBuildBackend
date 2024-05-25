package com.rebuild.backend.model.forms.resume_forms;

import com.rebuild.backend.model.constraints.entities.PhoneNumber;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record HeaderForm(@NotBlank String name,

                         @NotBlank
                         @Email
                         String email,

                         PhoneNumber number) {
}

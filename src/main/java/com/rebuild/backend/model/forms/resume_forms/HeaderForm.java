package com.rebuild.backend.model.forms.resume_forms;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record HeaderForm(@NotBlank(message = "Name may not be blank")
                         String name,
                         @NotBlank(message = "Email may not be blank")
                         @Email(message = "Must be a valid email")
                         String email,
                         String number,
                         List<String> links) {
}

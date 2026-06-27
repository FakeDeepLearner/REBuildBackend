package com.rebuild.backend.model.forms.resume_forms;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record HeaderForm(
                         String name,

                         String email,
                         String number,
                         List<String> links) {
}

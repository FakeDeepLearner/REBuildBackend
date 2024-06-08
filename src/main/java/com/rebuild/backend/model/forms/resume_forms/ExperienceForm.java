package com.rebuild.backend.model.forms.resume_forms;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.temporal.Temporal;
import java.util.List;

public record ExperienceForm(@NotBlank(message = "Company name may not be blank") String companyName,
                             Temporal startDate,
                             Temporal endDate,
                             @NotEmpty(message = "The bullets may not be empty") List<String> bullets) {
}

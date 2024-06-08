package com.rebuild.backend.model.forms.resume_forms;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ExperienceForm(@NotBlank(message = "Company name may not be blank") String companyName,
                             @NotBlank(message = "Time period may not be blank")  String timePeriod,
                             @NotEmpty(message = "The bullets may not be empty") List<String> bullets) {
}

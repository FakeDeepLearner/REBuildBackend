package com.rebuild.backend.model.forms.resume_forms;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ExperienceForm(@NotBlank String companyName,
                             @NotBlank  String timePeriod,
                             @NotEmpty List<String> bullets) {
}

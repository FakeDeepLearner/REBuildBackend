package com.rebuild.backend.model.forms.resume_forms;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ExperienceForm(@NotBlank(message = "Company name may not be blank") String companyName,

                             @NotEmpty(message = "The technologies may not be empty")
                             List<String> technologies,

                             String location,

                             String experienceType,
                             String startDate,
                             String endDate,
                             @NotEmpty(message = "The bullets may not be empty") List<String> bullets) {
}

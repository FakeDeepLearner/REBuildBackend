package com.rebuild.backend.model.forms.resume_forms;

import com.rebuild.backend.model.constraints.resume_and_profile.EndDateAfterStartDateConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.temporal.Temporal;
import java.util.List;

@EndDateAfterStartDateConstraint
public record ExperienceForm(@NotBlank(message = "Company name may not be blank") String companyName,

                             @NotEmpty(message = "The technologies may not be empty") List<String> technologies,
                             Temporal startDate,
                             Temporal endDate,
                             @NotEmpty(message = "The bullets may not be empty") List<String> bullets) {
}

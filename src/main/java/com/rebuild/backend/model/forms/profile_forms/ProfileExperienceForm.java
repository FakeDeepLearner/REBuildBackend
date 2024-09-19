package com.rebuild.backend.model.forms.profile_forms;

import com.rebuild.backend.model.constraints.resume_and_profile.EndDateAfterStartDateConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.temporal.Temporal;
import java.util.List;

@EndDateAfterStartDateConstraint
public record ProfileExperienceForm(@NotBlank(message = "Company name can't be empty") String companyName,
                                    @NotEmpty(message = "Technologies can't be empty") List<String> technologies,
                                    Temporal startDate,
                                    Temporal endDate,
                                   @NotEmpty(message = "Bullets can't be empty") List<String> bullets) {
}

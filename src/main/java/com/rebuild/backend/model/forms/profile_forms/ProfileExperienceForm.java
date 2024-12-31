package com.rebuild.backend.model.forms.profile_forms;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ProfileExperienceForm(@NotBlank(message = "Company name can't be empty") String companyName,
                                    @NotEmpty(message = "Technologies can't be empty") List<String> technologies,
                                    String startDate,
                                   String endDate,
                                   @NotEmpty(message = "Bullets can't be empty") List<String> bullets) {
}

package com.rebuild.backend.model.forms.profile_forms;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ProfileEducationForm(@NotBlank(message = "School name may not be blank") String schoolName,
                                   @NotEmpty(message = "Coursework can't be empty") List<String> relevantCoursework,
                                   String location,
                                   String startDate, String endDate) {
}

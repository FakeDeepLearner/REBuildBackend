package com.rebuild.backend.model.forms.resume_forms;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record EducationForm(@NotBlank(message = "The school name may not be blank")
                            String schoolName,
                            @NotEmpty(message = "The coursework may not be empty")
                            List<String> relevantCoursework,
                            String location,
                            String startDate,
                            String endDate) {
}

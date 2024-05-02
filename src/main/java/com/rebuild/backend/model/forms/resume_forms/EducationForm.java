package com.rebuild.backend.model.forms.resume_forms;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record EducationForm(@NotBlank
                            String schoolName,
                            @NotEmpty
                            List<String> relevantCoursework) {
}

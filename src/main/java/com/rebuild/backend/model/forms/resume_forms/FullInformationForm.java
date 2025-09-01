package com.rebuild.backend.model.forms.resume_forms;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record FullInformationForm(HeaderForm headerForm,

                             EducationForm educationForm,

                             @NotEmpty(message = "The experience list may not be empty")
                             List<ExperienceForm> experiences) {
}

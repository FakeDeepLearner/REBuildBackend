package com.rebuild.backend.model.forms.resume_forms;

import com.rebuild.backend.model.entities.resume_entities.Experience;
import com.rebuild.backend.model.entities.resume_entities.PhoneNumber;
import com.rebuild.backend.model.entities.resume_entities.ResumeSection;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record FullResumeForm(HeaderForm headerForm,

                             EducationForm educationForm,

                             @NotEmpty(message = "The experience list may not be empty")
                             List<ExperienceForm> experiences,

                             List<SectionForm> sections) {
}

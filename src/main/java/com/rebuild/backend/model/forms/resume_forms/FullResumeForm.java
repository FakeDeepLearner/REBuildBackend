package com.rebuild.backend.model.forms.resume_forms;

import com.rebuild.backend.model.entities.resume_entities.Experience;
import com.rebuild.backend.model.entities.resume_entities.PhoneNumber;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record FullResumeForm(@NotBlank(message = "Name may not be blank") String name,

                             @NotBlank(message = "Email may not be blank")
                             @Email(message = "Must be a valid email")
                             String email,

                             PhoneNumber phoneNumber,

                             @NotBlank(message = "The school name may not be blank")
                             String schoolName,

                             @NotEmpty(message = "The coursework may not be empty")
                             List<String> relevantCoursework,

                             @NotEmpty(message = "The experience list may not be empty")
                             List<Experience> experiences) {
}
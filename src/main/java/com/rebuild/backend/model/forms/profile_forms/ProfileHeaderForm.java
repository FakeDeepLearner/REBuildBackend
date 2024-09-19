package com.rebuild.backend.model.forms.profile_forms;

import com.rebuild.backend.model.entities.resume_entities.PhoneNumber;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ProfileHeaderForm(PhoneNumber number,
                                @NotBlank(message = "Name can't be empty") String name,
                                @Email(message = "Must be a valid email") String email) {
}

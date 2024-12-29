package com.rebuild.backend.model.forms.profile_forms;

import com.rebuild.backend.model.entities.resume_entities.PhoneNumber;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ProfileHeaderForm(PhoneNumber number,
                                @NotBlank(message = "First name can't be empty")
                                String firstName,
                                @NotBlank(message = "Last name may not be empty")
                                String lastName,
                                @Email(message = "Must be a valid email") String email) {
}

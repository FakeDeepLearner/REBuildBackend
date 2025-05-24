package com.rebuild.backend.model.forms.profile_forms;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ProfileHeaderForm(String number,
                                @NotBlank(message = "First name can't be empty")
                                String firstName,
                                @NotBlank(message = "Last name may not be empty")
                                String lastName,
                                @Email(message = "Must be a valid email") String email) {
}

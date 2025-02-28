package com.rebuild.backend.model.forms.profile_forms;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ProfileSectionForm(@NotBlank(message = "Title may not be blank") String title,
                                 @NotEmpty(message = "Entry forms may not be empty")
                                 List<ProfileSectionEntryForm> entryForms) {
}

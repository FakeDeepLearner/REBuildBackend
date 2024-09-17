package com.rebuild.backend.model.forms.profile_forms;

import com.rebuild.backend.model.entities.resume_entities.PhoneNumber;

public record ProfileHeaderForm(PhoneNumber number,
                                String name, String email) {
}

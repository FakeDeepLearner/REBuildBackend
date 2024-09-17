package com.rebuild.backend.model.forms.profile_forms;

import com.rebuild.backend.model.entities.profile_entities.ProfileExperience;
import com.rebuild.backend.model.entities.resume_entities.PhoneNumber;

import java.util.List;

public record FullProfileForm(PhoneNumber phoneNumber,
                              String name, String email,
                              String schoolName, List<String> relevantCoursework,
                              List<ProfileExperience> experiences) {
}

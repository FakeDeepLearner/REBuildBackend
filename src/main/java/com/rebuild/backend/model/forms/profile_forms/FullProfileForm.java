package com.rebuild.backend.model.forms.profile_forms;

import com.rebuild.backend.model.entities.profile_entities.ProfileExperience;
import com.rebuild.backend.model.entities.profile_entities.ProfileSection;
import com.rebuild.backend.model.entities.resume_entities.PhoneNumber;

import java.util.List;

public record FullProfileForm(ProfileHeaderForm headerForm,
                              ProfileEducationForm educationForm,
                              List<ProfileExperienceForm> experienceForms,
                              List<ProfileSectionForm> sectionForms) {
}

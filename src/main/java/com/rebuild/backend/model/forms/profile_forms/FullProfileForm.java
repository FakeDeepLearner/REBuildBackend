package com.rebuild.backend.model.forms.profile_forms;

import java.util.List;

public record FullProfileForm(ProfileHeaderForm headerForm,
                              ProfileEducationForm educationForm,
                              List<ProfileExperienceForm> experienceForms,
                              List<ProfileSectionForm> sectionForms) {
}

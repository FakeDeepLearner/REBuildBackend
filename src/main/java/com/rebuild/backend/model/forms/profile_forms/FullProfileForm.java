package com.rebuild.backend.model.forms.profile_forms;

import com.rebuild.backend.model.forms.resume_forms.FullInformationForm;

public record FullProfileForm(FullInformationForm fullInformationForm,
                              ProfilePreferencesForm preferencesForm) {
}

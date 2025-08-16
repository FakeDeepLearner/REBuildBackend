package com.rebuild.backend.model.forms.resume_forms;

public record VersionSwitchPreferencesForm(boolean includeHeader, boolean includeEducation,
                                           boolean includeExperiences, boolean includeSections) {
}

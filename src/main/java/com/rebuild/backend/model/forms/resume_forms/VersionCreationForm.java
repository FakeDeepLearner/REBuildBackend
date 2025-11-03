package com.rebuild.backend.model.forms.resume_forms;

public record VersionCreationForm(boolean includeName, boolean includeHeader,
                                  boolean includeEducation, boolean includeExperience) {
}

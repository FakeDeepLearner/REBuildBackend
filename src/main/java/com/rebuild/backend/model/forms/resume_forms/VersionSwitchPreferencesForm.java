package com.rebuild.backend.model.forms.resume_forms;

import java.util.List;

public record VersionSwitchPreferencesForm(boolean includeHeader, boolean includeEducation,
                                                 List<Integer> experienceIndices, List<Integer> sectionIndices,
                                                 boolean makeHeaderCopy, boolean makeEducationCopy,
                                                 boolean makeExperienceCopies){
}

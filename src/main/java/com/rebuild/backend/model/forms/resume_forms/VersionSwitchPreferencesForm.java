package com.rebuild.backend.model.forms.resume_forms;

import java.util.List;
import java.util.UUID;

public record VersionSwitchPreferencesForm(boolean includeHeader, boolean includeEducation,
                                           List<UUID> experienceIds,
                                           boolean makeHeaderCopy, boolean makeEducationCopy,
                                           boolean makeExperienceCopies){
}

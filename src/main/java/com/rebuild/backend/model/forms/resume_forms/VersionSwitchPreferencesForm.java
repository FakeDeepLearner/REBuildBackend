package com.rebuild.backend.model.forms.resume_forms;

import java.util.List;
import java.util.UUID;

public record VersionSwitchPreferencesForm(boolean includeName,
                                           boolean includeHeader, boolean includeEducation,
                                           List<UUID> experienceIds, List<UUID> projectIds,
                                           boolean makeHeaderCopy, boolean makeEducationCopy,
                                           boolean makeExperienceCopies, boolean makeProjectCopies){
}

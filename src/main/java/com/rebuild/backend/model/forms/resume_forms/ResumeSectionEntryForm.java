package com.rebuild.backend.model.forms.resume_forms;

import java.util.List;

public record ResumeSectionEntryForm(String title, List<String> toolsUsed,
                                     String location, String startTime, String endTime,
                                     List<String> bullets) {
}

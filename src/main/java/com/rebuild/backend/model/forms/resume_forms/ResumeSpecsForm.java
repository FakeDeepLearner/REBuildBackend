package com.rebuild.backend.model.forms.resume_forms;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
import java.time.LocalDateTime;

public record ResumeSpecsForm(String resumeNameContains,
                              String creationAfterCutoff,
                              String creationBeforeCutoff) {
}

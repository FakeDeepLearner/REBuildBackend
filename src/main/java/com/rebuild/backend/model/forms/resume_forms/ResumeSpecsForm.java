package com.rebuild.backend.model.forms.resume_forms;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
import java.time.LocalDateTime;

public record ResumeSpecsForm(String resumeNameContains, String firstNameContains,
                              String lastNameContains,
                              String creationAfterCutoff,
                              String creationBeforeCutoff,
                              String schoolNameContains,
                              String courseWorkContains,
                              String companyContains,
                              String experienceBulletsContains,
                              String experienceTechnologyListContains,
                              String projectNameContains,
                              String projectTechnologyListContains,
                              String projectBulletsContains) {
}

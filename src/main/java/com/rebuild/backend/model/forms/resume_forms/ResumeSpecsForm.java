package com.rebuild.backend.model.forms.resume_forms;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record ResumeSpecsForm(String resumeNameContains, String firstNameContains,
                              String lastNameContains,
                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                             LocalDateTime creationAfterCutoff,
                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                             LocalDateTime creationBeforeCutoff,
                              String schoolNameContains,
                              String courseWorkContains,
                              String companyContains,
                              String bulletsContains,
                              String technologyListContains) {
}

package com.rebuild.backend.model.forms.forum_forms;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record ForumSpecsForm(
                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                            LocalDateTime postAfterCutoff,
                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                            LocalDateTime postBeforeCutoff,
                            String titleContains,
                            String bodyContains) {
}

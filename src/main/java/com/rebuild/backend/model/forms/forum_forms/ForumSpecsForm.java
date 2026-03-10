package com.rebuild.backend.model.forms.forum_forms;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
import java.time.LocalDateTime;

public record ForumSpecsForm(String titleContains,
                            String bodyContains) {
}

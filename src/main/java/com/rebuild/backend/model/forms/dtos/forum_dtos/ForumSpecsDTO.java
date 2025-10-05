package com.rebuild.backend.model.forms.dtos.forum_dtos;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record ForumSpecsDTO(
                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                            LocalDateTime postAfterCutoff,
                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                            LocalDateTime postBeforeCutoff,
                            String titleContains,
                            String bodyContains) {
}

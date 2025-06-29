package com.rebuild.backend.model.forms.dtos.forum_dtos;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record ForumSpecsDTO(String postedUsername,
                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                            LocalDateTime postAfterCutoff,
                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                            LocalDateTime postBeforeCutoff,
                            String titleContains,
                            String bodyContains,
                            String titleStartsWith,
                            String bodyStartsWith,
                            String titleEndsWith,
                            String bodyEndsWith,
                            Integer bodyMinSize,
                            Integer bodyMaxSize) {
}

package com.rebuild.backend.model.dtos;

import java.time.Instant;

public record ProfileHistoryCommentDTO(String content, Instant createdAt) {
}

package com.rebuild.backend.model.dtos.user_dtos;

import java.time.Instant;

public record ProfileHistoryCommentDTO(String content, Instant createdAt) {
}

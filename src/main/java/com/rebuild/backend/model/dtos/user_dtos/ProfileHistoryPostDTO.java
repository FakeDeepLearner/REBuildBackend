package com.rebuild.backend.model.dtos.user_dtos;

import java.time.Instant;

public record ProfileHistoryPostDTO(String title, String content, Instant createdAt) {
}

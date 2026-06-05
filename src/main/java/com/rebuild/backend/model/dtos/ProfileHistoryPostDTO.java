package com.rebuild.backend.model.dtos;

import java.time.Instant;

public record ProfileHistoryPostDTO(String title, String content, Instant createdAt) {
}

package com.rebuild.backend.model.responses.forum_responses;

import java.time.Instant;

public record EditCommentResponse(String newContent, Instant newTime) {
}

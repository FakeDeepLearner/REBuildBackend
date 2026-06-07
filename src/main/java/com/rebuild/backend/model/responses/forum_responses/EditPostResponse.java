package com.rebuild.backend.model.responses.forum_responses;

import java.time.Instant;

public record EditPostResponse(String newTitle, String newContent, Instant newTime) {
}

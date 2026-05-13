package com.rebuild.backend.model.dtos.forum_dtos;

import java.time.Instant;
import java.util.UUID;

public record FriendRequestDTO(UUID requestId, String requestingUsername, Instant requestedAt) {
}

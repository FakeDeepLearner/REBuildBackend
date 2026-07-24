package com.rebuild.backend.model.dtos.user_dtos;

import java.time.Instant;
import java.util.UUID;

public record FriendRequestDTO(UUID requestId, String requestingUsername, Instant requestedAt) {
}

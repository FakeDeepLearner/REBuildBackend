package com.rebuild.backend.model.dtos.forum_dtos;

import java.time.Instant;
import java.util.UUID;

//Here so I don't accidentally delete this class
@SuppressWarnings("unused")
public record FriendRequestDTO(UUID requestId, String requestingUsername, Instant requestedAt) {
}

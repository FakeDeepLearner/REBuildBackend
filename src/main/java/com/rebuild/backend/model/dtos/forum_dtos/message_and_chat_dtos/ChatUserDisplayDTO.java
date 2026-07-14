package com.rebuild.backend.model.dtos.forum_dtos.message_and_chat_dtos;

import java.time.Instant;
import java.util.UUID;

public record ChatUserDisplayDTO(UUID userId, String username, Instant memberSince, boolean isAdmin,
                                 boolean isRequestingUser,
                                 String imageUrl) {
}

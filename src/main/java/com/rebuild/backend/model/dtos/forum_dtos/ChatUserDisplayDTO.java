package com.rebuild.backend.model.dtos.forum_dtos;

import java.time.Instant;
import java.util.UUID;

public record ChatUserDisplayDTO(UUID userId, String username, Instant memberSince, boolean isAdmin,
                                 boolean isOwner, boolean isRequestingUser,
                                 String imageUrl) {
}

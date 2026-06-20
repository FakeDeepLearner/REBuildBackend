package com.rebuild.backend.model.dtos.forum_dtos;

import java.time.Instant;
import java.util.UUID;

//TODO: Add the user profile picture URL here once that is implemented.
public record ChatUserDisplayDTO(UUID userId, String username, Instant memberSince, boolean isAdmin,
                                 boolean isOwner, boolean isRequestingUser) {
}

package com.rebuild.backend.model.dtos.user_dtos;

import java.util.UUID;

public record UserFriendDTO(UUID userId, String username, String profilePictureUrl) {
}

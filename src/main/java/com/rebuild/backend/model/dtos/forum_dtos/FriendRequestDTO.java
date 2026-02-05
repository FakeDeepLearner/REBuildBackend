package com.rebuild.backend.model.dtos.forum_dtos;

import com.rebuild.backend.model.entities.user_entities.User;

import java.util.UUID;

public record FriendRequestDTO(User sender, UUID recipientId) {
}

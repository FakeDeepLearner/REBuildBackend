package com.rebuild.backend.model.forms.dtos.forum_dtos;

import com.rebuild.backend.model.entities.users.Inbox;
import com.rebuild.backend.model.entities.users.User;

import java.util.UUID;

public record FriendRequestDTO(User sender, UUID recipientId) {
}

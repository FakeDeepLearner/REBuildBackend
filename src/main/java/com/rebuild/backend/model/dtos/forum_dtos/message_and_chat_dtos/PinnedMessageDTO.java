package com.rebuild.backend.model.dtos.forum_dtos.message_and_chat_dtos;

import java.time.Instant;
import java.util.UUID;

public record PinnedMessageDTO(UUID id, String senderUsername, String content, Instant createdAt,
                               String imageUrl) {
}

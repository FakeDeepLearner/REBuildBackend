package com.rebuild.backend.model.dtos.forum_dtos.message_and_chat_dtos;

import java.time.Instant;
import java.util.UUID;

public record MessageSearchDTO(UUID messageId, String senderUsername, String content,
                               Instant messageTime, boolean isEdited, boolean messageIsPinned,
                               String imageUrl) {
}

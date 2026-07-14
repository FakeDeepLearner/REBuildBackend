package com.rebuild.backend.model.dtos.forum_dtos.message_and_chat_dtos;

import java.time.Instant;
import java.util.UUID;

public record MessageDisplayDTO(UUID messageId, String senderUsername, String messageContent, Instant messageTime,
                                boolean displayOnTheRight, boolean messageIsRemoved, boolean messageIsEdited,
                                boolean messageIsPinned,
                                String imageUrl) {
}


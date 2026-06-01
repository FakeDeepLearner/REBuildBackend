package com.rebuild.backend.model.dtos.websocket_dtos;

import java.util.UUID;

public record NewChatDTO(UUID chatId, UUID senderId, UUID messageId, String content,
                         String senderName) {
}

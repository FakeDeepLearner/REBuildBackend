package com.rebuild.backend.model.dtos;

import java.time.Instant;
import java.util.UUID;

public record NewMessageNotificationDTO(UUID chatId, UUID senderId, UUID messageId,
                                        String messageContent, Instant sentAt,
                                        String senderName, String chatName) {
}

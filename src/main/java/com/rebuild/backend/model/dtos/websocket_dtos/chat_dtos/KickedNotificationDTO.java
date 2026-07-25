package com.rebuild.backend.model.dtos.websocket_dtos.chat_dtos;

import java.util.UUID;

public record KickedNotificationDTO(String chatName, UUID chatId) {
}

package com.rebuild.backend.model.responses;

import java.util.UUID;

public record DisplayChatResponse(UUID chatId, String username, String pictureUrl) {
}

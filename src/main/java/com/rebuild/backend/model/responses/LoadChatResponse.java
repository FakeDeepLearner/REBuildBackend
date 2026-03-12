package com.rebuild.backend.model.responses;

import com.rebuild.backend.model.dtos.forum_dtos.MessageDisplayDTO;

import java.util.List;
import java.util.UUID;

public record LoadChatResponse(String displayName, UUID chatId,
                               List<MessageDisplayDTO> messages, String profilePictureUrl) {
}

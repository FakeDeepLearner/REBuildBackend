package com.rebuild.backend.model.responses;

import com.rebuild.backend.model.forms.dtos.forum_dtos.MessageDisplayDTO;

import java.util.List;
import java.util.UUID;

public record LoadChatResponse(String username, UUID userId,
                               List<MessageDisplayDTO> messages, String profilePictureUrl) {
}

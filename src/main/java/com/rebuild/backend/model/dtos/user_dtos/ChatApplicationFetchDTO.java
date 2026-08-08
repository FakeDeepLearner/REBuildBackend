package com.rebuild.backend.model.dtos.user_dtos;

import java.time.Instant;
import java.util.UUID;

public record ChatApplicationFetchDTO(UUID id, String appliedChatName, String content,
                                     Instant creationTime) {

    public ChatApplicationDisplayDTO toDisplayDTO() {
        return new ChatApplicationDisplayDTO(id, appliedChatName, content,
                creationTime.toString());
    }
}

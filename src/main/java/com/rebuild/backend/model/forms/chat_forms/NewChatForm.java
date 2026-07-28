package com.rebuild.backend.model.forms.chat_forms;

import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.UUID;

public record NewChatForm(@NotBlank(message = "Chat name may not be blank") String chatName,
                          @NotBlank(message = "Chat description may not be blank") String chatDescription,
                          List<UUID> invitedUserIds) {
}

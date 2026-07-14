package com.rebuild.backend.model.dtos.forum_dtos.message_and_chat_dtos;

import com.rebuild.backend.model.entities.messaging_and_friendship_entities.Message;

import java.util.List;

public record FetchMessagesDTO(List<Message> messages, boolean hasMore) {
}

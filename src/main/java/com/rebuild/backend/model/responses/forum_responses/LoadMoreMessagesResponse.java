package com.rebuild.backend.model.responses.forum_responses;

import com.rebuild.backend.model.dtos.forum_dtos.message_and_chat_dtos.MessageDisplayDTO;

import java.time.Instant;
import java.util.List;

public record LoadMoreMessagesResponse(List<MessageDisplayDTO> newMessages,
                                       boolean hasMore) {
}

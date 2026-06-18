package com.rebuild.backend.model.responses.forum_responses;

import com.rebuild.backend.model.dtos.forum_dtos.MessageSearchDTO;

import java.util.List;

public record SearchMessagesResponse(List<MessageSearchDTO> displayedMessages, boolean hasNext) {
}

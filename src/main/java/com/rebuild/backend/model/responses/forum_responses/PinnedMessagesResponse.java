package com.rebuild.backend.model.responses.forum_responses;

import com.rebuild.backend.model.dtos.forum_dtos.PinnedMessageDTO;

import java.util.List;

public record PinnedMessagesResponse(List<PinnedMessageDTO> pinnedMessages, boolean hasNext) {
}

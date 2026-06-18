package com.rebuild.backend.model.responses.forum_responses;

import com.rebuild.backend.model.dtos.forum_dtos.MessageDisplayDTO;

import java.time.Instant;
import java.util.List;

public record MessageJumpResponse(List<MessageDisplayDTO> displayedMessages,
                                  boolean hasMoreFromTop, boolean hasMoreFromBottom,
                                  Instant lastFromTopTime, Instant lastFromBottomTime) {
}

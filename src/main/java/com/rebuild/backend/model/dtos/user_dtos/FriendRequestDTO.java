package com.rebuild.backend.model.dtos.user_dtos;

import com.rebuild.backend.model.responses.user_responses.FriendRequestResponse;

import java.time.Instant;
import java.util.UUID;

public record FriendRequestDTO(UUID requestId, String requestingUsername, Instant requestedAt,
                               String requestContent) {

    public FriendRequestResponse convertToResponse()
    {
        return new FriendRequestResponse(requestId, requestingUsername, requestedAt.toString(), requestContent);
    }
}

package com.rebuild.backend.model.responses.user_responses;

import java.util.UUID;

public record FriendRequestResponse(UUID requestId, String username, String requestedAt, String content) {
}

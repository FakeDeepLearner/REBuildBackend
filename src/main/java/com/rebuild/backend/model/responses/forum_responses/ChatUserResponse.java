package com.rebuild.backend.model.responses.forum_responses;

import java.util.UUID;

public record ChatUserResponse(UUID userId, String username, boolean userIsAdmin, boolean userIsOwner,
                               boolean userIsRequestingUser, boolean hasNext,
                               boolean requestingUserIsOwer, boolean requestingUserIsAdmin) {
}

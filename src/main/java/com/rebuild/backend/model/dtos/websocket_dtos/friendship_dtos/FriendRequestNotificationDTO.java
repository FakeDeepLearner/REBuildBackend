package com.rebuild.backend.model.dtos.websocket_dtos.friendship_dtos;

import java.time.Instant;
import java.util.UUID;

public record FriendRequestNotificationDTO(String invitingUsername, UUID invitationId,
                                           String sentTime) {
}

package com.rebuild.backend.model.dtos;

import java.time.Instant;
import java.util.UUID;

public record FriendRequestNotificationDTO(String invitingUsername, UUID invitationId,
                                           Instant sentTime) {
}

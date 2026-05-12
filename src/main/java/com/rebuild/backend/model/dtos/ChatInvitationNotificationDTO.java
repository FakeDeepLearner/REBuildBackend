package com.rebuild.backend.model.dtos;

import java.time.Instant;
import java.util.UUID;

public record ChatInvitationNotificationDTO(String invitingUsername, String chatName,
                                            UUID invitationId, Instant sentTime) {
}

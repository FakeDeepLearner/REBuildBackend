package com.rebuild.backend.model.dtos.websocket_dtos.friendship_dtos;

public record FriendRequestActionDTO(String actioningUsername,
                                     boolean accepted) {
}

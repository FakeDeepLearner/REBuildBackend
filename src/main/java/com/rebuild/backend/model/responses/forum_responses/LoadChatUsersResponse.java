package com.rebuild.backend.model.responses.forum_responses;

import com.rebuild.backend.model.dtos.forum_dtos.ChatUserDisplayDTO;

import java.util.List;

public record LoadChatUsersResponse(List<ChatUserDisplayDTO> userDisplays,
                                    boolean requestingUserIsAdmin, boolean requestingUserIsOwner) {
}

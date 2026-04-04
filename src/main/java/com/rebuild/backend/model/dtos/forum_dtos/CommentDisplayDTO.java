package com.rebuild.backend.model.dtos.forum_dtos;

import java.util.UUID;

public record CommentDisplayDTO(UUID commentID, String content, String authorUsername, int replyCount,
                                boolean userHasLikedComment) {
}

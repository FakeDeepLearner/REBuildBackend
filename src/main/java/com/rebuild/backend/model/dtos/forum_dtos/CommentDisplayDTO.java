package com.rebuild.backend.model.dtos.forum_dtos;

import java.time.Instant;
import java.util.UUID;

public record CommentDisplayDTO(UUID commentID, String content, String displayedName,
                                Instant displayedTime,
                                int replyCount,
                                boolean authorIsPostingUser,
                                boolean isDeleted,
                                boolean isEdited) {
}

package com.rebuild.backend.model.dtos.forum_dtos.comment_and_post_dtos;

import java.time.Instant;
import java.util.UUID;

public record CommentDisplayDTO(UUID commentID, String content, String displayedName,
                                Instant displayedTime,
                                int replyCount,
                                boolean authorIsPostingUser,
                                boolean isDeleted,
                                boolean isEdited) {
}

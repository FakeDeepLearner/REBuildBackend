package com.rebuild.backend.model.forms.dtos.forum_dtos;

import java.util.UUID;

public record CommentDisplayDTO(UUID commentID, String content, String authorUsername, int replyCount) {
}

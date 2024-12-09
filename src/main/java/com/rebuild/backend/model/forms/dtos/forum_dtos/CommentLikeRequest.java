package com.rebuild.backend.model.forms.dtos.forum_dtos;

import java.util.UUID;

public record CommentLikeRequest(String likingUserEmail,
                                 UUID likedCommentId) {
}

package com.rebuild.backend.model.forms.dtos.forum_dtos;

import java.util.UUID;

public record CommentReplyLikeRequest(String likingUserUsername, UUID commentReplyId) {
}

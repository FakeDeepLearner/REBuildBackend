package com.rebuild.backend.model.forms.dtos.forum_dtos;

import java.util.UUID;

public record PostLikeRequest(String likingUserUsername, UUID likedPostId) {
}

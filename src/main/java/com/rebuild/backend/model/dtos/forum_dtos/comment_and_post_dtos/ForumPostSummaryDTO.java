package com.rebuild.backend.model.dtos.forum_dtos.comment_and_post_dtos;

import java.util.UUID;

public record ForumPostSummaryDTO(UUID id, String title, String content, int commentCount){
}

package com.rebuild.backend.model.dtos.forum_dtos;

import java.util.UUID;

public record ForumPostSummaryDTO(UUID id, String title, String content, int likeCount, int commentCount,
                                  boolean userHasLikedPost){
}

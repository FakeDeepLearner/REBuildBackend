package com.rebuild.backend.model.responses;

import com.rebuild.backend.model.dtos.forum_dtos.ForumPostSummaryDTO;
import com.rebuild.backend.model.entities.forum_entities.ForumPost;

import java.util.List;

public record ForumPostPageResponse(List<ForumPostSummaryDTO> displayedPosts, int currentPage,
                                    boolean hasNext) {
}

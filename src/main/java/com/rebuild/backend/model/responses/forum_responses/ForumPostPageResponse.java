package com.rebuild.backend.model.responses.forum_responses;

import com.rebuild.backend.model.dtos.forum_dtos.ForumPostSummaryDTO;

import java.util.List;

public record ForumPostPageResponse(List<ForumPostSummaryDTO> displayedPosts, int currentPage,
                                    boolean hasNext) {
}

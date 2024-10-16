package com.rebuild.backend.model.responses;

import com.rebuild.backend.model.entities.forum_entities.ForumPost;

import java.util.List;

public record ForumPostPageResponse(List<ForumPost> displayedPosts, int currentPage,
                                    long totalItems, int totalPages,
                                    String linkToPrevPage, String linkToNextPage) {
}

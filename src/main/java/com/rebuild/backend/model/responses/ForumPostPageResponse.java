package com.rebuild.backend.model.responses;

import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.entities.forum_entities.PostSearchConfiguration;

import java.util.List;

public record ForumPostPageResponse(List<ForumPost> displayedPosts, int currentPage,
                                    long totalItems, int totalPages,
                                    int pageSize, String searchToken,
                                    List<PostSearchConfiguration> searchConfigurations) {
}

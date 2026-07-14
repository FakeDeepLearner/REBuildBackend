package com.rebuild.backend.model.responses.forum_responses;

import com.rebuild.backend.model.dtos.forum_dtos.comment_and_post_dtos.CommentDisplayDTO;

import java.util.List;

public record LoadCommentsResponse(List<CommentDisplayDTO> comments, int currentPage, boolean hasNext) {
}

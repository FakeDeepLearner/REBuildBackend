package com.rebuild.backend.model.dtos.forum_dtos.comment_and_post_dtos;


import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PostDisplayDTO(UUID postId, String title, String content,
                             String authorUsername, Instant postTime, List<?> resumes,
                             List<CommentDisplayDTO> displayedComments,
                             boolean hasMoreComments) {
}

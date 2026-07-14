package com.rebuild.backend.model.dtos.forum_dtos.comment_and_post_dtos;

import com.rebuild.backend.model.responses.resume_responses.ResumePreviewResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PostDisplayDTO(UUID postId, String title, String content,
                             String authorUsername, Instant postTime, List<ResumePreviewResponse> resumes,
                             List<CommentDisplayDTO> displayedComments,
                             boolean hasMoreComments) {
}

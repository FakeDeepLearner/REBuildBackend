package com.rebuild.backend.model.dtos.forum_dtos;

import com.rebuild.backend.model.entities.forum_entities.PostResume;

import java.util.List;
import java.util.UUID;

public record PostDisplayDTO(UUID postId, String title, String content,
                             String authorUsername, List<PostResume> resumes,
                             List<CommentDisplayDTO> displayedComments, int currentCommentPage,
                             boolean hasMoreComments,
                             List<String> fileUploadURLs, boolean userHasLikedPost) {
}

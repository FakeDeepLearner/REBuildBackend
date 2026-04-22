package com.rebuild.backend.model.dtos.forum_dtos;

import java.util.UUID;

public record CommentFetchDTO(UUID authorId, UUID associatedPostId, UUID commentID, String content, String authorUsername, int replyCount,
                              boolean userHasLikedComment) {

    public CommentDisplayDTO toDisplayDto(boolean isUserOriginalPoster){
        return new CommentDisplayDTO(this.commentID, this.content, this.authorUsername,
                this.replyCount, isUserOriginalPoster, this.userHasLikedComment);
    }
}

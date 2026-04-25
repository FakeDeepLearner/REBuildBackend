package com.rebuild.backend.model.dtos.forum_dtos;

import com.rebuild.backend.utils.StringUtil;

import java.util.UUID;

public record CommentFetchDTO(UUID authorId, UUID associatedPostId, UUID commentID, String content, String authorUsername, int replyCount,
                              boolean userHasLikedComment, boolean commentIsDeleted, boolean commentIsAnonymized,
                              String anonymizedBaseName) {

    private String determineDisplayedContent()
    {
        if (!this.commentIsDeleted)
        {
            return this.content;
        }
        return "Comment has been removed";
    }

    private String determineDisplayedUsername()
    {
        if (!this.commentIsAnonymized)
        {
            return this.authorUsername;
        }
        return StringUtil.getAnonymizedName(this.anonymizedBaseName, this.associatedPostId);
    }

    public CommentDisplayDTO toDisplayDto(boolean isUserOriginalPoster){
        return new CommentDisplayDTO(this.commentID,
                determineDisplayedContent(), determineDisplayedUsername(),
                this.replyCount, isUserOriginalPoster, this.userHasLikedComment, this.commentIsDeleted);
    }
}

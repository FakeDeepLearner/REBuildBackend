package com.rebuild.backend.model.dtos.forum_dtos;

import com.rebuild.backend.utils.StringUtil;

import java.time.Instant;
import java.util.UUID;

public record CommentFetchDTO(UUID authorId, UUID associatedPostId, UUID commentID, String content, String authorUsername, int replyCount,
                              boolean userHasLikedComment, boolean commentIsDeleted, boolean commentIsAnonymized,
                              String anonymizedBaseName, Instant creationTime, Instant modificationTime,
                              boolean commentIsEdited) {

    private String determineDisplayedContent()
    {
        if (!this.commentIsDeleted)
        {
            return this.content;
        }
        return "Comment has been removed";
    }

    private String determineDisplayedAuthor()
    {
        if (commentIsDeleted)
        {
            return "[Removed]";
        }
        return StringUtil.determineDisplayedCommentName(commentIsAnonymized, authorUsername,
                anonymizedBaseName, associatedPostId);
    }

    public CommentDisplayDTO toDisplayDto(boolean isUserOriginalPoster){
        return new CommentDisplayDTO(commentIsDeleted ? null : commentID,
                determineDisplayedContent(), determineDisplayedAuthor(),
                commentIsEdited ? modificationTime : creationTime,
                this.replyCount, isUserOriginalPoster, this.userHasLikedComment, this.commentIsDeleted);
    }
}

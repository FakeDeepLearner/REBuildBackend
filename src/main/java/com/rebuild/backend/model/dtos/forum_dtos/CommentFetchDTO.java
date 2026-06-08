package com.rebuild.backend.model.dtos.forum_dtos;

import com.rebuild.backend.utils.StringUtil;

import java.time.Instant;
import java.util.UUID;

public record CommentFetchDTO(UUID authorId, UUID associatedPostId, UUID commentID, String content, String authorUsername, int replyCount,
                              int likesCount, boolean userHasLikedComment, boolean commentIsDeleted,
                              boolean commentIsAnonymized, String anonymizedBaseName,
                              Instant creationTime, Instant modificationTime,
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

    private Instant determineDisplayedCreationTime()
    {
        if (commentIsDeleted)
        {
            return null;
        }

        return commentIsEdited ? modificationTime : creationTime;
    }


    public CommentDisplayDTO toDisplayDto(boolean isUserOriginalPoster){
        boolean editedDisplay = !commentIsDeleted && commentIsEdited;
        UUID displayedId = commentIsDeleted ? null : commentID;
        return new CommentDisplayDTO(displayedId,
                determineDisplayedContent(), determineDisplayedAuthor(),
                determineDisplayedCreationTime(),
                replyCount, likesCount, isUserOriginalPoster,
                userHasLikedComment, commentIsDeleted,
                editedDisplay);
    }
}

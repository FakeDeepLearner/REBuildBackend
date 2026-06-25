package com.rebuild.backend.model.dtos.forum_dtos;

import com.rebuild.backend.utils.StringUtil;

import java.time.Instant;
import java.util.UUID;

public record CommentFetchDTO(UUID authorId, UUID associatedPostId, UUID commentID, String content, String authorUsername, int replyCount,
                              boolean commentIsDeleted,
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
                anonymizedBaseName);
    }

    private Instant determineDisplayedCreationTime()
    {
        if (commentIsDeleted)
        {
            return null;
        }

        return commentIsEdited ? modificationTime : creationTime;
    }

    private boolean determineUserIsOriginalPoster(String postAuthorName)
    {
        if (commentIsAnonymized || commentIsDeleted)
        {
            return false;
        }
        return postAuthorName.equals(authorUsername);
    }


    public CommentDisplayDTO toDisplayDto(String postAuthorUsername){
        boolean editedDisplay = !commentIsDeleted && commentIsEdited;
        UUID displayedId = commentIsDeleted ? null : commentID;
        return new CommentDisplayDTO(displayedId,
                determineDisplayedContent(), determineDisplayedAuthor(),
                determineDisplayedCreationTime(),
                replyCount, determineUserIsOriginalPoster(postAuthorUsername), commentIsDeleted,
                editedDisplay);
    }
}

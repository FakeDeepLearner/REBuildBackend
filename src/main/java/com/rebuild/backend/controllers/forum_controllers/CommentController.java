package com.rebuild.backend.controllers.forum_controllers;

import com.rebuild.backend.exceptions.forum_exceptions.CommentForbiddenException;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.entities.forum_entities.Comment;
import com.rebuild.backend.service.forum_services.ForumPostAndCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/forum/comments")
public class CommentController {

    private final ForumPostAndCommentService forumPostAndCommentService;

    @Autowired
    public CommentController(ForumPostAndCommentService forumPostAndCommentService) {
        this.forumPostAndCommentService = forumPostAndCommentService;
    }

    @PostMapping("/create/{post_id}")
    @ResponseStatus(HttpStatus.CREATED)
    public Comment createTopLevelComment(@PathVariable UUID post_id, @RequestBody String commentBody,
                                         @AuthenticationPrincipal User creatingUser){
        return forumPostAndCommentService.makeTopLevelComment(commentBody, post_id, creatingUser);
    }

    @PostMapping("/reply/{comment_id}")
    @ResponseStatus(HttpStatus.CREATED)
    public Comment createReply(@PathVariable UUID comment_id, @RequestBody String replyBody,
                                @AuthenticationPrincipal User creatingUser){
        return forumPostAndCommentService.createReplyTo(replyBody, comment_id, creatingUser);
    }

    @DeleteMapping("/delete/{comment_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable UUID comment_id,
                              @AuthenticationPrincipal User deletingUser){
        if(!forumPostAndCommentService.commentBelongsToUser(comment_id, deletingUser.getId())){
            throw new CommentForbiddenException("That comment doesn't belong to you, you can't delete it");
        }
        forumPostAndCommentService.deleteComment(comment_id);
    }

}

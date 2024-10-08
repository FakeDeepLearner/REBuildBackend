package com.rebuild.backend.controllers.forum_controllers;

import com.rebuild.backend.exceptions.forum_exceptions.CommentForbiddenException;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.entities.forum_entities.Comment;
import com.rebuild.backend.service.ForumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final ForumService forumService;

    @Autowired
    public CommentController(ForumService forumService) {
        this.forumService = forumService;
    }

    @PostMapping("/create/{post_id}")
    public Comment createTopLevelComment(@PathVariable UUID post_id, @RequestBody String commentBody,
                                         @AuthenticationPrincipal UserDetails initialDetails){
        //Since we have only one class that implements UserDetails, we can safely do casting here
        User actualUser = (User) initialDetails;
        return forumService.makeTopLevelComment(commentBody, post_id, actualUser);
    }

    @PostMapping("/reply/{comment_id}")
    public Comment createReply(@PathVariable UUID comment_id, @RequestBody String replyBody,
                                @AuthenticationPrincipal UserDetails initialDetails){
        User actualUser = (User) initialDetails;
        return forumService.createReplyTo(replyBody, comment_id, actualUser);
    }

    @DeleteMapping("/delete/{comment_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable UUID comment_id,
                              @AuthenticationPrincipal UserDetails deletingDetails){
        User deletingUser = (User) deletingDetails;
        if(!forumService.commentBelongsToUser(comment_id, deletingUser.getId())){
            throw new CommentForbiddenException("That comment doesn't belong to you, you can't delete it");
        }
        forumService.deleteComment(comment_id);
    }

}

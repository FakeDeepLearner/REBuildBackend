package com.rebuild.backend.controllers.forum_controllers;

import com.rebuild.backend.exceptions.forum_exceptions.CommentForbiddenException;
import com.rebuild.backend.model.entities.forum_entities.CommentReply;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.entities.forum_entities.Comment;
import com.rebuild.backend.model.forms.forum_forms.CommentForm;
import com.rebuild.backend.service.forum_services.ForumPostAndCommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public Comment createTopLevelComment(@PathVariable UUID post_id, @RequestBody @Valid CommentForm commentForm,
                                         @AuthenticationPrincipal User creatingUser){
        return forumPostAndCommentService.makeTopLevelComment(commentForm, post_id, creatingUser);
    }

    @PostMapping("/reply/{top_level_comment_id}/{parent_reply_id}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentReply createReply(@PathVariable UUID top_level_comment_id,
                                    @PathVariable(required = false) UUID parent_reply_id,
                                    @RequestBody @Valid CommentForm commentForm,
                                    @AuthenticationPrincipal User creatingUser){
        return forumPostAndCommentService.createReplyTo(top_level_comment_id, parent_reply_id, creatingUser, commentForm);
    }

    @GetMapping("/{top_level_comment_id}/replies")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentReply> getReplies(@PathVariable UUID top_level_comment_id){
        return null;
    }

    @GetMapping("/{top_level_comment_id}/{reply_id}/expand")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentReply> expandReplies(){
        return null;
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

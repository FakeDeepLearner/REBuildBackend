package com.rebuild.backend.controllers.forum_controllers;

import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.entities.forum_entities.Comment;
import com.rebuild.backend.model.dtos.forum_dtos.CommentDisplayDTO;
import com.rebuild.backend.model.forms.forum_forms.CommentForm;
import com.rebuild.backend.service.forum_services.CommentsService;
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

    private final CommentsService commentsService;

    @Autowired
    public CommentController(CommentsService commentsService) {
        this.commentsService = commentsService;
    }

    @PostMapping("/create/{post_id}")
    @ResponseStatus(HttpStatus.CREATED)
    public Comment createTopLevelComment(@PathVariable UUID post_id, @RequestBody @Valid CommentForm commentForm,
                                         @AuthenticationPrincipal User creatingUser){
        return commentsService.makeTopLevelComment(commentForm, post_id, creatingUser);
    }

    @PostMapping("/reply/{top_level_comment_id}")
    @ResponseStatus(HttpStatus.CREATED)
    public Comment createReply(@PathVariable UUID top_level_comment_id,
                                    @RequestBody @Valid CommentForm commentForm,
                                    @AuthenticationPrincipal User creatingUser){
        return commentsService.createReplyTo(top_level_comment_id, creatingUser, commentForm);
    }


    @GetMapping("/{parent_comment_id}/replies")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentDisplayDTO> getReplies(@PathVariable UUID parent_comment_id,
                                              @AuthenticationPrincipal User user){
        return commentsService.getCommentExpansionInfo(parent_comment_id, user);
    }


    @DeleteMapping("/delete/{comment_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable UUID comment_id,
                              @AuthenticationPrincipal User deletingUser){
        commentsService.deleteComment(comment_id, deletingUser);
    }

}

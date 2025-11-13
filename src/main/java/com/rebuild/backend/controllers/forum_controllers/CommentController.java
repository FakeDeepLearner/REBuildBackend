package com.rebuild.backend.controllers.forum_controllers;

import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.entities.forum_entities.Comment;
import com.rebuild.backend.model.forms.dtos.forum_dtos.CommentDisplayDTO;
import com.rebuild.backend.model.forms.forum_forms.CommentForm;
import com.rebuild.backend.service.forum_services.ForumPostAndCommentService;
import com.rebuild.backend.utils.database_utils.UserContext;
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

    @PostMapping("/reply/{top_level_comment_id}")
    @ResponseStatus(HttpStatus.CREATED)
    public Comment createReply(@PathVariable UUID top_level_comment_id,
                                    @RequestBody @Valid CommentForm commentForm,
                                    @AuthenticationPrincipal User creatingUser){
        return forumPostAndCommentService.createReplyTo(top_level_comment_id, creatingUser, commentForm);
    }


    @GetMapping("/{parent_comment_id}/replies")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentDisplayDTO> getReplies(@PathVariable UUID parent_comment_id){
        return forumPostAndCommentService.getCommentExpansionInfo(parent_comment_id);
    }


    @DeleteMapping("/delete/{comment_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable UUID comment_id,
                              @AuthenticationPrincipal User deletingUser){
        UserContext.set(deletingUser.getId());
        forumPostAndCommentService.deleteComment(comment_id);
        UserContext.clear();
    }

}

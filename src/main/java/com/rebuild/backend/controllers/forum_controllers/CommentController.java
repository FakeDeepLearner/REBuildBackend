package com.rebuild.backend.controllers.forum_controllers;

import com.rebuild.backend.model.forms.forum_forms.EditPostForm;
import com.rebuild.backend.model.responses.forum_responses.EditCommentResponse;
import com.rebuild.backend.model.responses.forum_responses.EditPostResponse;
import com.rebuild.backend.model.responses.forum_responses.LoadCommentsResponse;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.dtos.forum_dtos.CommentDisplayDTO;
import com.rebuild.backend.model.forms.forum_forms.CommentForm;
import com.rebuild.backend.service.forum_services.CommentsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/forum/comments")
public class CommentController {

    private final CommentsService commentsService;

    @Autowired
    public CommentController(CommentsService commentsService) {
        this.commentsService = commentsService;
    }

    @PostMapping("/new_comment/{parent_id}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDisplayDTO createComment(@PathVariable UUID parent_id, @RequestBody @Valid CommentForm commentForm,
                                           @AuthenticationPrincipal User creatingUser){
        return commentsService.createComment(commentForm, parent_id, creatingUser);
    }

    @GetMapping("/{post_id}/{parent_comment_id}/replies")
    @ResponseStatus(HttpStatus.OK)
    public LoadCommentsResponse getReplies(@PathVariable UUID post_id,
                                           @PathVariable UUID parent_comment_id,
                                           @RequestParam(defaultValue = "0", name = "page") int pageNumber,
                                           @AuthenticationPrincipal User user){
        return commentsService.obtainMoreComments(post_id, parent_comment_id, user, pageNumber);
    }


    @GetMapping("/load-comments/{post_id}")
    @ResponseStatus(HttpStatus.OK)
    public LoadCommentsResponse loadMorePostComments(@PathVariable UUID post_id, @AuthenticationPrincipal User user,
                                                     @RequestParam(defaultValue = "0", name = "page") int pageNumber)
    {
        return commentsService.obtainMoreComments(post_id, null, user, pageNumber);
    }


    @DeleteMapping("/delete/{comment_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable UUID comment_id,
                              @AuthenticationPrincipal User deletingUser){
        commentsService.deleteComment(comment_id, deletingUser);
    }

    @PatchMapping("/flip-anonymization/{commentId}")
    public String changeCommentAnonymization(@AuthenticationPrincipal User user,
                                             @PathVariable UUID commentId){
        return commentsService.changeCommentAnonymization(commentId, user);
    }

    @PatchMapping("edit-post/{post_id}")
    @ResponseStatus(HttpStatus.OK)
    public EditCommentResponse editComment(@AuthenticationPrincipal User user, @PathVariable UUID post_id,
                                        @RequestBody String newContent) {
        return commentsService.editComment(post_id, user, newContent);
    }

}

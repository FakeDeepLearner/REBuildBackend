package com.rebuild.backend.controllers.forum_controllers;

import com.rebuild.backend.model.dtos.forum_dtos.CommentDisplayDTO;
import com.rebuild.backend.model.dtos.forum_dtos.PostDisplayDTO;
import com.rebuild.backend.model.entities.forum_entities.Comment;
import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.service.forum_services.CommentsService;
import com.rebuild.backend.service.forum_services.PostsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/forum/batch")
public class ForumLikesController {

    private final CommentsService commentsService;

    private final PostsService postsService;

    @Autowired
    public ForumLikesController(CommentsService commentsService, PostsService postsService) {
        this.commentsService = commentsService;
        this.postsService = postsService;
    }



    @PostMapping("/like_comment/{comment_id}")
    public boolean triggerCommentLikeStatus(@AuthenticationPrincipal User likingUser,
                                                      @PathVariable UUID comment_id){
        return commentsService.likeComment(comment_id, likingUser);

    }

    @PostMapping("/like_post/{post_id}")
    public boolean triggerPostLikeStatus(@AuthenticationPrincipal User likingUser,
                                                @PathVariable UUID post_id){
        return postsService.likePost(post_id, likingUser);
    }

}

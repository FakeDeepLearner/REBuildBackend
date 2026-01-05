package com.rebuild.backend.controllers.forum_controllers;

import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.forms.dtos.forum_dtos.CommentLikeRequest;
import com.rebuild.backend.model.forms.dtos.forum_dtos.CommentReplyLikeRequest;
import com.rebuild.backend.model.forms.dtos.forum_dtos.PostLikeRequest;
import com.rebuild.backend.service.util_services.RabbitProducingService;
import org.springframework.amqp.AmqpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;

import java.util.UUID;

@RestController
@RequestMapping("/api/forum/batch")
public class ForumLikesController {

    private final RabbitProducingService producingService;

    @Autowired
    public ForumLikesController(RabbitProducingService producingService) {
        this.producingService = producingService;
    }

    @PostMapping("/like_comment/{comment_id}")
    public ResponseEntity<String> likeComment(@AuthenticationPrincipal User likingUser,
                                              @PathVariable UUID comment_id){
        try{
            CommentLikeRequest newRequest = new CommentLikeRequest(likingUser.getForumUsername(), comment_id);
            producingService.sendCommentLike(newRequest);
            return ResponseEntity.ok("Comment liked");
        }
        catch (AmqpException amqpException){
            return ResponseEntity.internalServerError().body(amqpException.getMessage());
        }
    }

    @PostMapping("/like_post/{post_id}")
    public ResponseEntity<String> likePost(@AuthenticationPrincipal User likingUser,
                                           @PathVariable UUID post_id){
        try{
            PostLikeRequest newRequest = new PostLikeRequest(likingUser.getForumUsername(), post_id);
            producingService.sendPostLike(newRequest);
            return ResponseEntity.ok("Comment liked");
        }
        catch (AmqpException amqpException){
            return ResponseEntity.internalServerError().body(amqpException.getMessage());
        }
    }

}

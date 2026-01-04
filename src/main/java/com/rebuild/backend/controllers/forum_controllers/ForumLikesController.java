package com.rebuild.backend.controllers.forum_controllers;

import com.rebuild.backend.model.forms.dtos.forum_dtos.CommentLikeRequest;
import com.rebuild.backend.model.forms.dtos.forum_dtos.CommentReplyLikeRequest;
import com.rebuild.backend.model.forms.dtos.forum_dtos.PostLikeRequest;
import com.rebuild.backend.service.util_services.RabbitProducingService;
import org.springframework.amqp.AmqpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;

@RestController
@RequestMapping("/api/forum/batch")
public class ForumLikesController {

    private final RabbitProducingService producingService;

    @Autowired
    public ForumLikesController(RabbitProducingService producingService) {
        this.producingService = producingService;
    }

    @PostMapping("/like_comment")
    public ResponseEntity<String> likeComment(@RequestBody CommentLikeRequest likeRequest){
        try{
            producingService.sendCommentLike(likeRequest);
            return ResponseEntity.ok("Comment liked");
        }
        catch (AmqpException amqpException){
            return ResponseEntity.internalServerError().body(amqpException.getMessage());
        }
    }

    @PostMapping("/like_post")
    public ResponseEntity<String> likePost(@RequestBody PostLikeRequest likeRequest){
        try{
            producingService.sendPostLike(likeRequest);
            return ResponseEntity.ok("Comment liked");
        }
        catch (AmqpException amqpException){
            return ResponseEntity.internalServerError().body(amqpException.getMessage());
        }
    }

}

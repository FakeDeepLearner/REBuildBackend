package com.rebuild.backend.service.util_services;

import com.rebuild.backend.model.forms.dtos.forum_dtos.CommentLikeRequest;
import com.rebuild.backend.model.forms.dtos.forum_dtos.CommentReplyLikeRequest;
import com.rebuild.backend.model.forms.dtos.forum_dtos.PostLikeRequest;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitProducingService {

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public RabbitProducingService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendPostLike(PostLikeRequest postLikeRequest){
        rabbitTemplate.convertAndSend("likesExchange",
                "postLikesRoutingKey", postLikeRequest);

    }

    public void sendCommentLike(CommentLikeRequest commentLikeRequest){
        rabbitTemplate.convertAndSend("likesExchange",
                "commentLikesRoutingKey", commentLikeRequest);

    }

    public void sendReplyLike(CommentReplyLikeRequest replyLikeRequest){
        rabbitTemplate.convertAndSend("likesExchange", "replyLikesRoutingKey",
                replyLikeRequest);
    }



}

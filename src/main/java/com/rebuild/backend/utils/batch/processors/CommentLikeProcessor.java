package com.rebuild.backend.utils.batch.processors;

import com.rebuild.backend.model.entities.forum_entities.Comment;
import com.rebuild.backend.model.entities.forum_entities.CommentLike;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.forms.dtos.forum_dtos.CommentLikeRequest;
import com.rebuild.backend.repository.CommentRepository;
import com.rebuild.backend.service.user_services.UserService;
import lombok.NonNull;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommentLikeProcessor implements ItemProcessor<CommentLikeRequest, CommentLike> {

    private final UserService userService;

    private final CommentRepository commentRepository;

    @Autowired
    public CommentLikeProcessor(UserService userService, CommentRepository commentRepository) {
        this.userService = userService;
        this.commentRepository = commentRepository;
    }

    @Override
    public CommentLike process(@NonNull CommentLikeRequest item){

        return new CommentLike(item.likedCommentId(), item.likingUserEmail());
    }
}

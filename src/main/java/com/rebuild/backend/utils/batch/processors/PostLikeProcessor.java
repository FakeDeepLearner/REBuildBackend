package com.rebuild.backend.utils.batch.processors;

import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.entities.forum_entities.PostLike;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.forms.dtos.forum_dtos.PostLikeRequest;
import com.rebuild.backend.repository.ForumPostRepository;
import com.rebuild.backend.service.user_services.UserService;
import lombok.NonNull;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PostLikeProcessor implements ItemProcessor<PostLikeRequest, PostLike> {

    private final UserService userService;

    private final ForumPostRepository postRepository;

    @Autowired
    public PostLikeProcessor(UserService userService, ForumPostRepository postRepository) {
        this.userService = userService;
        this.postRepository = postRepository;
    }

    @Override
    public PostLike process(@NonNull PostLikeRequest item) throws Exception {
        return new PostLike(item.likedPostId(), item.likingUserEmail());
    }
}

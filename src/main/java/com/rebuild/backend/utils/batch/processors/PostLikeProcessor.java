package com.rebuild.backend.utils.batch.processors;

import com.rebuild.backend.model.entities.forum_entities.Comment;
import com.rebuild.backend.model.entities.forum_entities.CommentLike;
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
        User likingUser = userService.findByEmailNoOptional(item.likingUserEmail());
        ForumPost likedPost = postRepository.findById(item.likedPostId()).orElse(null);
        assert likedPost != null;
        return new PostLike(likedPost, likingUser);
    }
}

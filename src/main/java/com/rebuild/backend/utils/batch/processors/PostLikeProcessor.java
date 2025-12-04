package com.rebuild.backend.utils.batch.processors;

import com.rebuild.backend.model.entities.forum_entities.LikeType;
import com.rebuild.backend.model.entities.forum_entities.Like;
import com.rebuild.backend.model.forms.dtos.forum_dtos.PostLikeRequest;
import lombok.NonNull;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;

@Component
public class PostLikeProcessor implements ItemProcessor<PostLikeRequest, Like> {

    @Override
    public Like process(@NonNull PostLikeRequest item) throws Exception {
        return new Like(item.likingUserUsername(), item.likedPostId(), LikeType.POST, Instant.now());
    }
}

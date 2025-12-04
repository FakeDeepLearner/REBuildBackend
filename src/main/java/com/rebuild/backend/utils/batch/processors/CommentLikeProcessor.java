package com.rebuild.backend.utils.batch.processors;

import com.rebuild.backend.model.entities.forum_entities.LikeType;
import com.rebuild.backend.model.entities.forum_entities.Like;
import com.rebuild.backend.model.forms.dtos.forum_dtos.CommentLikeRequest;
import lombok.NonNull;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;

@Component
public class CommentLikeProcessor implements ItemProcessor<CommentLikeRequest, Like> {

    @Override
    public Like process(@NonNull CommentLikeRequest item){

        return new Like(item.likingUserUsername(), item.likedCommentId(), LikeType.COMMENT, Instant.now());
    }
}

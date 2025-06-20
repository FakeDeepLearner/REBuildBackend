package com.rebuild.backend.utils.batch.processors;

import com.rebuild.backend.model.entities.enums.LikeType;
import com.rebuild.backend.model.entities.forum_entities.Like;
import com.rebuild.backend.model.forms.dtos.forum_dtos.CommentReplyLikeRequest;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ReplyLikeProcessor implements ItemProcessor<CommentReplyLikeRequest, Like> {

    @Override
    public Like process(CommentReplyLikeRequest item) throws Exception {
        return new Like(item.likingUserUsername(), item.commentReplyId(), LikeType.COMMENT_REPLY, LocalDateTime.now());
    }
}

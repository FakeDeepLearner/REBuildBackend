package com.rebuild.backend.utils.batch.writers;

import com.rebuild.backend.model.entities.forum_entities.CommentLike;
import com.rebuild.backend.repository.CommentLikeRepository;
import com.rebuild.backend.repository.CommentRepository;
import lombok.NonNull;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommentsWriter implements ItemWriter<CommentLike> {

    private final CommentLikeRepository commentLikeRepository;

    @Autowired
    public CommentsWriter(CommentLikeRepository commentLikeRepository) {
        this.commentLikeRepository = commentLikeRepository;
    }

    @Override
    public void write(@NonNull Chunk<? extends CommentLike> chunk) throws Exception {
        commentLikeRepository.saveAll(chunk);
    }
}

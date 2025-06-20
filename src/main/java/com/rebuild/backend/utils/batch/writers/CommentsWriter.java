package com.rebuild.backend.utils.batch.writers;

import com.rebuild.backend.model.entities.forum_entities.Like;
import com.rebuild.backend.repository.LikeRepository;
import lombok.NonNull;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommentsWriter implements ItemWriter<Like> {

    private final LikeRepository likeRepository;

    @Autowired
    public CommentsWriter(LikeRepository likeRepository) {
        this.likeRepository = likeRepository;
    }

    @Override
    public void write(@NonNull Chunk<? extends Like> chunk) {
        likeRepository.saveAll(chunk);
    }
}

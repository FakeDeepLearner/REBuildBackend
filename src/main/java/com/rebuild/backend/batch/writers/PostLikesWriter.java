package com.rebuild.backend.batch.writers;

import com.rebuild.backend.model.entities.forum_entities.Like;
import com.rebuild.backend.repository.forum_repositories.LikeRepository;
import lombok.NonNull;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PostLikesWriter implements ItemWriter<Like> {

    private final LikeRepository likeRepository;

    @Autowired
    public PostLikesWriter(LikeRepository likeRepository) {
        this.likeRepository = likeRepository;
    }


    @Override
    @Transactional
    public void write(@NonNull Chunk<? extends Like> chunk) {
        likeRepository.saveAll(chunk);
    }
}

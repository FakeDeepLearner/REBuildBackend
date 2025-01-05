package com.rebuild.backend.utils.batch.writers;

import com.rebuild.backend.model.entities.forum_entities.PostLike;
import com.rebuild.backend.repository.PostLikeRepository;
import lombok.NonNull;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PostLikesWriter implements ItemWriter<PostLike> {

    private final PostLikeRepository postLikeRepository;

    @Autowired
    public PostLikesWriter(PostLikeRepository postLikeRepository) {
        this.postLikeRepository = postLikeRepository;
    }


    @Override
    @Transactional
    public void write(@NonNull Chunk<? extends PostLike> chunk) throws Exception {
        postLikeRepository.saveAll(chunk);
    }
}

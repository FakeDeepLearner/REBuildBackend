package com.rebuild.backend.batch.writers;

import com.rebuild.backend.model.entities.forum_entities.Comment;
import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.forms.dtos.forum_dtos.LikesUpdateDTO;
import com.rebuild.backend.repository.forum_repositories.CommentRepository;
import com.rebuild.backend.repository.forum_repositories.ForumPostRepository;
import lombok.NonNull;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LikeUpdateWriter implements ItemWriter<LikesUpdateDTO> {

    private final ForumPostRepository postRepository;

    private final CommentRepository commentRepository;

    @Autowired
    public LikeUpdateWriter(ForumPostRepository postRepository,
                            CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public void write(@NonNull Chunk<? extends LikesUpdateDTO> chunk) throws Exception {
        for (LikesUpdateDTO likesUpdateDTO : chunk.getItems()) {
            switch (likesUpdateDTO.typeOfTarget()){
                case COMMENT -> {
                    Comment comment = commentRepository.findById(likesUpdateDTO.targetObjectId()).orElseThrow();
                    comment.setLikeCount((int) (comment.getLikeCount() + likesUpdateDTO.numItemsRead()));
                    commentRepository.saveAndFlush(comment);
                }

                case POST -> {
                    ForumPost post = postRepository.findById(likesUpdateDTO.targetObjectId()).orElseThrow();
                    post.setLikeCount((int) (post.getLikeCount() + likesUpdateDTO.numItemsRead()));
                    postRepository.saveAndFlush(post);

                }

            }
        }
    }
}

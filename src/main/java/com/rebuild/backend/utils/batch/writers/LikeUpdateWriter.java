package com.rebuild.backend.utils.batch.writers;

import com.rebuild.backend.model.entities.forum_entities.Comment;
import com.rebuild.backend.model.entities.forum_entities.CommentReply;
import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.forms.dtos.forum_dtos.LikesUpdateDTO;
import com.rebuild.backend.repository.CommentReplyRepository;
import com.rebuild.backend.repository.CommentRepository;
import com.rebuild.backend.repository.ForumPostRepository;
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

    private final CommentReplyRepository commentReplyRepository;

    @Autowired
    public LikeUpdateWriter(ForumPostRepository postRepository,
                            CommentRepository commentRepository,
                            CommentReplyRepository commentReplyRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.commentReplyRepository = commentReplyRepository;
    }

    @Override
    public void write(@NonNull Chunk<? extends LikesUpdateDTO> chunk) throws Exception {
        List<ForumPost> updatedPosts = new ArrayList<>();
        List<Comment> updatedComments = new ArrayList<>();
        List<CommentReply> updatedCommentReplies = new ArrayList<>();
        for (LikesUpdateDTO likesUpdateDTO : chunk.getItems()) {
            switch (likesUpdateDTO.typeOfTarget()){
                case COMMENT -> {
                    Comment comment = commentRepository.findById(likesUpdateDTO.targetObjectId()).orElseThrow();

                    comment.setLikeCount((int) (comment.getLikeCount() + likesUpdateDTO.numItemsRead()));
                    updatedComments.add(comment);
                }

                case POST -> {
                    ForumPost post = postRepository.findById(likesUpdateDTO.targetObjectId()).orElseThrow();

                    post.setLikeCount((int) (post.getLikeCount() + likesUpdateDTO.numItemsRead()));
                    updatedPosts.add(post);

                }

                case COMMENT_REPLY -> {
                    CommentReply reply = commentReplyRepository.findById(likesUpdateDTO.targetObjectId()).orElseThrow();

                    reply.setLikeCount((int) (reply.getLikeCount() + likesUpdateDTO.numItemsRead()));
                    updatedCommentReplies.add(reply);
                }
            }
        }

        // Doing the transactions this way is a lot more efficient. We only have 1 connection to the database
        // instead of potentially hundreds.
        commentRepository.saveAll(updatedComments);
        postRepository.saveAll(updatedPosts);
        commentReplyRepository.saveAll(updatedCommentReplies);

    }
}

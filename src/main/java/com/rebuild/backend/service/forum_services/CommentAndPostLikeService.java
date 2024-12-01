package com.rebuild.backend.service.forum_services;

import com.rebuild.backend.model.entities.forum_entities.Comment;
import com.rebuild.backend.model.entities.forum_entities.CommentLike;
import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.entities.forum_entities.PostLike;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.repository.CommentLikeRepository;
import com.rebuild.backend.repository.PostLikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CommentAndPostLikeService {

    private final CommentLikeRepository commentLikeRepository;

    private final PostLikeRepository postLikeRepository;

    @Autowired
    public CommentAndPostLikeService(CommentLikeRepository commentLikeRepository,
                                     PostLikeRepository postLikeRepository) {
        this.commentLikeRepository = commentLikeRepository;
        this.postLikeRepository = postLikeRepository;
    }

    private boolean userHasLikedComment(User user, UUID commentID) {
        return commentLikeRepository.findByLikingUserAndLikedCommentId(user, commentID).isPresent();
    }

    private boolean userHasLikedPost(User user, UUID postID) {
        return postLikeRepository.findByLikingUserAndLikedPostId(user, postID).isPresent();
    }

    public CommentLike likeComment(User likingUser, Comment likedComment) {
        if (!userHasLikedComment(likingUser, likedComment.getId())) {
            return commentLikeRepository.save(new CommentLike(likedComment, likingUser));
        }

        return null;
    }

    public PostLike likePost(User likingUser, ForumPost likedPost){
        if (!userHasLikedPost(likingUser, likedPost.getId())) {
            return postLikeRepository.save(new PostLike(likedPost, likingUser));
        }
        return null;
    }



}

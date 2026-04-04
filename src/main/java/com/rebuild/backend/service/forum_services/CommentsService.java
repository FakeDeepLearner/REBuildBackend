package com.rebuild.backend.service.forum_services;

import com.rebuild.backend.model.dtos.forum_dtos.CommentDisplayDTO;
import com.rebuild.backend.model.entities.forum_entities.Comment;
import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.entities.forum_entities.Like;
import com.rebuild.backend.model.entities.forum_entities.LikeType;
import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.exceptions.BelongingException;
import com.rebuild.backend.model.exceptions.NotFoundException;
import com.rebuild.backend.model.forms.forum_forms.CommentForm;
import com.rebuild.backend.repository.forum_repositories.CommentRepository;
import com.rebuild.backend.repository.forum_repositories.ForumPostRepository;
import com.rebuild.backend.repository.forum_repositories.LikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CommentsService {

    private final CommentRepository commentRepository;

    private final ForumPostRepository postRepository;

    private final LikeRepository likeRepository;

    @Autowired
    public CommentsService(CommentRepository commentRepository, ForumPostRepository postRepository, LikeRepository likeRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.likeRepository = likeRepository;
    }

    @Transactional
    public void deleteComment(UUID commentID, User deletingUser){
        Comment commentToDelete = commentRepository.findByIdAndAuthor(commentID, deletingUser).orElseThrow(
                () -> new BelongingException("This comment does not belong to you")
        );
        commentRepository.delete(commentToDelete);
    }

    @Transactional
    public Comment makeTopLevelComment(CommentForm commentForm, UUID post_id, User creatingUser){
        ForumPost post = postRepository.findByIdWithComments(post_id).orElseThrow(
                () -> new NotFoundException("Post with the specified id is not found")
        );
        post.setCommentCount(post.getCommentCount() + 1);
        Comment newComment = new Comment(commentForm.content());
        newComment.setAssociatedPost(post);
        post.getComments().add(newComment);
        newComment.setParent(null);

        UserProfile profile = creatingUser.getUserProfile();
        profile.getMadeComments().add(newComment);
        newComment.setAssociatedProfile(profile);
        return commentRepository.save(newComment);

    }

    @Transactional
    public Comment createReplyTo(UUID parent_comment_id, User creatingUser,
                                 CommentForm commentForm){
        Comment parentComment = commentRepository.findById(parent_comment_id).
                orElseThrow(() -> new NotFoundException("Comment with the specified id not found"));

        Comment newComment = new Comment(commentForm.content());
        newComment.setAssociatedPost(parentComment.getAssociatedPost());
        newComment.setParent(parentComment);
        parentComment.setRepliesCount(parentComment.getRepliesCount() + 1);


        UserProfile profile = creatingUser.getUserProfile();
        newComment.setAssociatedProfile(profile);
        profile.getMadeComments().add(newComment);

        
        return commentRepository.save(newComment);
    }

    public List<CommentDisplayDTO> getCommentExpansionInfo(UUID parent_id, User user)
    {
        return commentRepository.loadParentCommentInfo(parent_id, user.getId());
    }


    public Comment likeComment(UUID comment_id, User likingUser)
    {
        Comment comment = commentRepository.findById(comment_id).orElseThrow(
                () -> new NotFoundException("Comment with this id is not found"));

        Optional<Like> foundLike = likeRepository.findByLikedObjectIdAndLikingUserId(comment_id,
                likingUser.getId());

        //If the user has already liked this comment, remove the like.
        foundLike.ifPresent(like -> {
            likeRepository.delete(like);
            comment.setLikeCount(comment.getLikeCount() - 1);
        });

        //If the user has not like this comment, simply add a like for this comment for this user.

        Like newLike = new Like(likingUser.getId(), comment_id, LikeType.COMMENT);

        likeRepository.save(newLike);
        comment.setLikeCount(comment.getLikeCount() + 1);

        return commentRepository.save(comment);
    }
}

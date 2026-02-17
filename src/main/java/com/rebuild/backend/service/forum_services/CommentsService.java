package com.rebuild.backend.service.forum_services;

import com.rebuild.backend.model.dtos.forum_dtos.CommentDisplayDTO;
import com.rebuild.backend.model.entities.forum_entities.Comment;
import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.exceptions.BelongingException;
import com.rebuild.backend.model.forms.forum_forms.CommentForm;
import com.rebuild.backend.repository.forum_repositories.CommentRepository;
import com.rebuild.backend.repository.forum_repositories.ForumPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CommentsService {

    private final CommentRepository commentRepository;

    private final ForumPostRepository postRepository;

    @Autowired
    public CommentsService(CommentRepository commentRepository, ForumPostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
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
        ForumPost post = postRepository.findByIdWithComments(post_id).orElseThrow(RuntimeException::new);
        post.setCommentCount(post.getCommentCount() + 1);
        Comment newComment = new Comment(commentForm.content());
        newComment.setAssociatedPost(post);
        post.getComments().add(newComment);
        newComment.setParent(null);
        // creatingUser.getMadeComments().add(newComment);
        newComment.setAuthor(creatingUser);
        return commentRepository.save(newComment);

    }

    @Transactional
    public Comment createReplyTo(UUID parent_comment_id, User creatingUser,
                                 CommentForm commentForm){
        Comment parentComment = commentRepository.findById(parent_comment_id).
                orElseThrow(RuntimeException::new);

        Comment newComment = new Comment(commentForm.content());
        newComment.setAuthor(creatingUser);
        newComment.setAssociatedPost(parentComment.getAssociatedPost());
        newComment.setParent(parentComment);
        parentComment.setRepliesCount(parentComment.getRepliesCount() + 1);


        return commentRepository.save(newComment);
    }

    public List<CommentDisplayDTO> getCommentExpansionInfo(UUID parent_id)
    {
        return commentRepository.loadParentCommentInfo(parent_id);
    }
}

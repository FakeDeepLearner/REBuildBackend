package com.rebuild.backend.service;

import com.rebuild.backend.model.entities.User;
import com.rebuild.backend.model.entities.forum_entities.Comment;
import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.repository.CommentRepository;
import com.rebuild.backend.repository.ForumPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ForumService {

    private final CommentRepository commentRepository;

    private final ForumPostRepository postRepository;

    @Autowired
    public ForumService(CommentRepository commentRepository, ForumPostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
    }

    public ForumPost createNewPost(String title, String content,
                                   Resume associatedResume,
                                   User creatingUser){
        ForumPost newPost = new ForumPost(title, content);
        newPost.setResume(associatedResume);
        newPost.setCreatingUser(creatingUser);
        creatingUser.getMadePosts().add(newPost);
        return postRepository.save(newPost);
    }

    public Comment makeTopLevelComment(String content, UUID post_id, User creatingUser){
        ForumPost post = postRepository.findById(post_id).orElseThrow(RuntimeException::new);
        Comment newComment = new Comment(content);
        newComment.setParentComment(null);
        newComment.setAssociatedPost(post);
        post.getComments().add(newComment);
        creatingUser.getMadeComments().add(newComment);
        newComment.setAuthor(creatingUser);
        return commentRepository.save(newComment);

    }

    public Comment createReplyTo(String content, UUID commend_id, User creatingUser){
        Comment parentComment = commentRepository.findById(commend_id).orElseThrow(RuntimeException::new);
        Comment newComment = new Comment(content);
        newComment.setParentComment(parentComment);
        newComment.setAssociatedPost(parentComment.getAssociatedPost());
        creatingUser.getMadeComments().add(newComment);
        newComment.setAuthor(creatingUser);
        return commentRepository.save(newComment);
    }

}

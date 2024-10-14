package com.rebuild.backend.service.forum_services;

import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.entities.forum_entities.Comment;
import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.repository.CommentRepository;
import com.rebuild.backend.repository.ForumPostRepository;
import com.rebuild.backend.service.resume_services.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ForumPostAndCommentService {

    private final ResumeService resumeService;

    private final CommentRepository commentRepository;

    private final ForumPostRepository postRepository;

    @Autowired
    public ForumPostAndCommentService(ResumeService resumeService,
                                      CommentRepository commentRepository,
                                      ForumPostRepository postRepository) {
        this.resumeService = resumeService;
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
    }

    public ForumPost createNewPost(String title, String content,
                                   UUID resumeID,
                                   User creatingUser){
        ForumPost newPost = new ForumPost(title, content);
        Resume associatedResume = resumeService.findById(resumeID);
        newPost.setResume(associatedResume);
        newPost.setCreatingUser(creatingUser);
        creatingUser.getMadePosts().add(newPost);
        return postRepository.save(newPost);
    }

    public void deletePost(UUID postID){
        postRepository.deleteById(postID);
    }

    public void deleteComment(UUID commentID){
        commentRepository.deleteById(commentID);
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

    public boolean postBelongsToUser(UUID postID, UUID userID){
        return postRepository.countByIdAndUserId(postID, userID) > 0;
    }

    public boolean commentBelongsToUser(UUID commentID, UUID userID){
        return commentRepository.countByIdAndUserId(commentID, userID) > 0;
    }



}

package com.rebuild.backend.service.forum_services;

import com.rebuild.backend.config.properties.AppUrlBase;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.entities.forum_entities.Comment;
import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.forms.dtos.forum_dtos.ForumSpecsDTO;
import com.rebuild.backend.model.responses.ForumPostPageResponse;
import com.rebuild.backend.repository.CommentRepository;
import com.rebuild.backend.repository.ForumPostRepository;
import com.rebuild.backend.service.resume_services.ResumeService;
import com.rebuild.backend.utils.specs.ForumPostSpecifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ForumPostAndCommentService {

    private final ResumeService resumeService;

    private final CommentRepository commentRepository;

    private final ForumPostRepository postRepository;
    private final ForumPostRepository forumPostRepository;

    private final AppUrlBase base;

    @Autowired
    public ForumPostAndCommentService(ResumeService resumeService,
                                      CommentRepository commentRepository,
                                      ForumPostRepository postRepository, ForumPostRepository forumPostRepository, AppUrlBase base) {
        this.resumeService = resumeService;
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.forumPostRepository = forumPostRepository;
        this.base = base;
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

    private Specification<ForumPost> deriveSpecification(String username,
                                                         LocalDateTime latest, LocalDateTime earliest,
                                                         String titleHas, String bodyHas,
                                                         String titleStarts,
                                                         String bodyStarts,
                                                         String bodyEnds,
                                                         String titleEnds,
                                                         Integer bodyMinSize,
                                                         Integer bodyMaxSize){
        List<Specification<ForumPost>> basicSpecs = new ArrayList<>();

        if(username != null){
            basicSpecs.add(ForumPostSpecifications.isPostedBy(username));
        }
        if(latest != null){
            basicSpecs.add(ForumPostSpecifications.isPostedBefore(latest));
        }
        if(earliest != null){
            basicSpecs.add(ForumPostSpecifications.isPostedAfter(earliest));
        }
        if(titleHas != null){
            basicSpecs.add(ForumPostSpecifications.titleContains(titleHas));
        }
        if(bodyHas != null){
            basicSpecs.add(ForumPostSpecifications.bodyContains(bodyHas));
        }

        if(titleStarts != null){
            basicSpecs.add(ForumPostSpecifications.titleStartsWith(titleStarts));
        }

        if(bodyStarts != null){
            basicSpecs.add(ForumPostSpecifications.bodyStartsWith(bodyStarts));
        }

        if(bodyEnds != null){
            basicSpecs.add(ForumPostSpecifications.bodyEndsWith(bodyEnds));
        }

        if(titleEnds != null){
            basicSpecs.add(ForumPostSpecifications.titleEndsWith(titleEnds));
        }

        if(bodyMinSize != null){
            basicSpecs.add(ForumPostSpecifications.bodyMinSize(bodyMinSize));
        }

        if(bodyMaxSize != null){
            basicSpecs.add(ForumPostSpecifications.bodyMaxSize(bodyMaxSize));
        }

        return Specification.allOf(basicSpecs);

    }

    public ForumPostPageResponse getPageResponses(int currentPageNumber, int pageSize,
                                                  ForumSpecsDTO forumSpecsDTO){
        Specification<ForumPost> derivedSpecification = deriveSpecification(
                forumSpecsDTO.postedUsername(), forumSpecsDTO.postAfterCutoff(), forumSpecsDTO.postBeforeCutoff(),
                forumSpecsDTO.titleContains(), forumSpecsDTO.bodyContains(),
                forumSpecsDTO.titleStartsWith(), forumSpecsDTO.bodyStartsWith(),
                forumSpecsDTO.titleEndsWith(), forumSpecsDTO.bodyEndsWith(),
                forumSpecsDTO.bodyMinSize(), forumSpecsDTO.bodyMaxSize());
        //Sort by descending order of creation dates, so the newest posts show up first
        Pageable pageableResult = PageRequest.of(currentPageNumber, pageSize,
                Sort.by("creationDate").descending().
                        //Break any ties by the last modified dates
                        and(Sort.by("lastModifiedDate").descending()));
        Page<ForumPost> resultingPage = forumPostRepository.findAll(derivedSpecification, pageableResult);
        return new ForumPostPageResponse(resultingPage.getContent(), resultingPage.getNumber(),
                resultingPage.getTotalElements(), resultingPage.getTotalPages(), pageSize);
    }



}

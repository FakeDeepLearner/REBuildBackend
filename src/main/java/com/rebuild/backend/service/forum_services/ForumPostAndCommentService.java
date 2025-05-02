package com.rebuild.backend.service.forum_services;

import com.rebuild.backend.config.properties.AppUrlBase;
import com.rebuild.backend.model.entities.forum_entities.CommentReply;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.entities.forum_entities.Comment;
import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.forms.dtos.forum_dtos.ForumSpecsDTO;
import com.rebuild.backend.model.forms.forum_forms.CommentForm;
import com.rebuild.backend.model.forms.forum_forms.NewPostForm;
import com.rebuild.backend.model.responses.ForumPostPageResponse;
import com.rebuild.backend.repository.CommentReplyRepository;
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
@Transactional(readOnly = true)
public class ForumPostAndCommentService {
    private final static String ANONYMOUS_USER = "Anonymous";

    private final ResumeService resumeService;

    private final CommentRepository commentRepository;

    private final CommentReplyRepository replyRepository;

    private final ForumPostRepository postRepository;

    private final AppUrlBase base;
    private final CommentReplyRepository commentReplyRepository;

    @Autowired
    public ForumPostAndCommentService(ResumeService resumeService,
                                      CommentRepository commentRepository, CommentReplyRepository replyRepository,
                                      ForumPostRepository postRepository, AppUrlBase base, CommentReplyRepository commentReplyRepository) {
        this.resumeService = resumeService;
        this.commentRepository = commentRepository;
        this.replyRepository = replyRepository;
        this.postRepository = postRepository;
        this.base = base;
        this.commentReplyRepository = commentReplyRepository;
    }

    @Transactional
    public ForumPost createNewPost(NewPostForm postForm,
                                   UUID resumeID,
                                   User creatingUser){
        String displayedUsername = determineDisplayedUsername(creatingUser, postForm.remainAnonymous());
        ForumPost newPost = new ForumPost(postForm.title(), postForm.content(), displayedUsername);
        Resume associatedResume = resumeService.findById(resumeID);
        newPost.setResume(associatedResume);
        newPost.setCreatingUser(creatingUser);
        creatingUser.getMadePosts().add(newPost);
        return postRepository.save(newPost);
    }

    @Transactional
    public void deletePost(UUID postID){
        postRepository.deleteById(postID);
    }

    @Transactional
    public void deleteComment(UUID commentID){
        commentRepository.deleteById(commentID);
    }

    @Transactional
    public Comment makeTopLevelComment(CommentForm commentForm, UUID post_id, User creatingUser){
        String displayedUsername = determineDisplayedUsername(creatingUser, commentForm.remainAnonymous());
        ForumPost post = postRepository.findById(post_id).orElseThrow(RuntimeException::new);
        post.setCommentCount(post.getCommentCount() + 1);
        Comment newComment = new Comment(commentForm.content(), displayedUsername);
        newComment.setAssociatedPost(post);
        post.getComments().add(newComment);
        creatingUser.getMadeComments().add(newComment);
        newComment.setAuthor(creatingUser);
        return commentRepository.save(newComment);

    }

    @Transactional
    public CommentReply createReplyTo(UUID top_level_comment_id, UUID parent_reply_id, User creatingUser,
                                      CommentForm commentForm){
        String displayedUsername = determineDisplayedUsername(creatingUser, commentForm.remainAnonymous());
        Comment topLevelComment = commentRepository.findById(top_level_comment_id).
                orElseThrow(RuntimeException::new);
        CommentReply parentReply = null;

        if(parent_reply_id != null){
            parentReply = commentReplyRepository.
                    findByParentReplyId(parent_reply_id).orElse(null);
        }

        //If we have a parent comment reply, we increase its number of children
        if(parentReply != null){
            parentReply.setChildRepliesCount(parentReply.getChildRepliesCount() + 1);
        }
        //If we don't have a parent comment, this means that this is a top level reply, and we are
        // a children of the top level actual comment directly.
        else{
            topLevelComment.setRepliesCount(topLevelComment.getRepliesCount() + 1);
        }
        CommentReply newReply = new CommentReply(commentForm.content(), displayedUsername);
        newReply.setAuthor(creatingUser);
        creatingUser.getMadeReplies().add(newReply);
        newReply.setParentReply(parentReply);
        newReply.setTopLevelComment(topLevelComment);
        return commentReplyRepository.save(newReply);
    }

    private String determineDisplayedUsername(User creatingUser, boolean anonymity){
        return anonymity ? ANONYMOUS_USER : creatingUser.getForumUsername();
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
        Page<ForumPost> resultingPage = postRepository.findAll(derivedSpecification, pageableResult);
        return new ForumPostPageResponse(resultingPage.getContent(), resultingPage.getNumber(),
                resultingPage.getTotalElements(), resultingPage.getTotalPages(), pageSize);
    }



}

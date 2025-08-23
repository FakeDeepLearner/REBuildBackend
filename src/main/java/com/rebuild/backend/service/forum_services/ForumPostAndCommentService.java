package com.rebuild.backend.service.forum_services;

import com.rebuild.backend.model.entities.forum_entities.CommentReply;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.entities.forum_entities.Comment;
import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.forms.dtos.forum_dtos.CommentDisplayDTO;
import com.rebuild.backend.model.forms.dtos.forum_dtos.ForumSpecsDTO;
import com.rebuild.backend.model.forms.dtos.forum_dtos.PostDisplayDTO;
import com.rebuild.backend.model.forms.forum_forms.CommentForm;
import com.rebuild.backend.model.forms.forum_forms.NewPostForm;
import com.rebuild.backend.model.responses.ForumPostPageResponse;
import com.rebuild.backend.repository.CommentReplyRepository;
import com.rebuild.backend.repository.CommentRepository;
import com.rebuild.backend.repository.ForumPostRepository;
import com.rebuild.backend.service.resume_services.ResumeService;
import com.rebuild.backend.specs.ForumPostSpecifications;
import com.rebuild.backend.specs.SpecificationBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ForumPostAndCommentService {
    private final static String ANONYMOUS_USER = "Anonymous";

    private final ResumeService resumeService;

    private final CommentRepository commentRepository;

    private final ForumPostRepository postRepository;

    private final CommentReplyRepository commentReplyRepository;

    private final JobLauncher jobLauncher;


    @Autowired
    public ForumPostAndCommentService(ResumeService resumeService,
                                      CommentRepository commentRepository, ForumPostRepository postRepository,
                                      CommentReplyRepository commentReplyRepository,
                                      @Qualifier("jobLauncher") JobLauncher jobLauncher) {
        this.resumeService = resumeService;
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.commentReplyRepository = commentReplyRepository;
        this.jobLauncher = jobLauncher;
    }

    @Transactional
    public ForumPost createNewPost(NewPostForm postForm,
                                   int resumeIndex,
                                   User creatingUser){
        String displayedUsername = determineDisplayedUsername(creatingUser, postForm.remainAnonymous());
        ForumPost newPost = new ForumPost(postForm.title(), postForm.content(), displayedUsername);
        Resume associatedResume = resumeService.findByUserIndex(creatingUser, resumeIndex);
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

    private Specification<ForumPost> deriveSpecification(ForumSpecsDTO specsDTO){

        List<SpecificationBuilder> specificationBuilders = List.of(
            input -> input.postedUsername() != null ? ForumPostSpecifications.isPostedBy(input.postedUsername()) : null,
                input -> input.postAfterCutoff() != null ? ForumPostSpecifications.isPostedBefore(input.postAfterCutoff()) : null,
                input -> input.postBeforeCutoff() != null ? ForumPostSpecifications.isPostedAfter(input.postBeforeCutoff()) : null,
                input -> input.titleContains() != null ? ForumPostSpecifications.titleContains(input.titleContains()) : null,
                input -> input.bodyContains() != null ? ForumPostSpecifications.bodyContains(input.bodyContains()) : null,
                input -> input.titleStartsWith() != null ? ForumPostSpecifications.titleStartsWith(input.titleStartsWith()) : null,
                input -> input.titleEndsWith() != null ? ForumPostSpecifications.titleEndsWith(input.titleEndsWith()) : null,
                input -> input.bodyStartsWith() != null ? ForumPostSpecifications.bodyStartsWith(input.bodyStartsWith()) : null,
                input -> input.bodyEndsWith() != null ? ForumPostSpecifications.bodyEndsWith(input.bodyEndsWith()) : null,
                input -> input.bodyMinSize() != null ? ForumPostSpecifications.bodyMinSize(input.bodyMinSize()) : null,
                input -> input.bodyMaxSize() != null ? ForumPostSpecifications.bodyMaxSize(input.bodyMaxSize()) : null
        );

        return Specification.allOf(
                specificationBuilders.stream()
                        .map(builder -> builder.buildSpecification(specsDTO))
                        .filter(Objects::nonNull).toList());

    }

    public ForumPostPageResponse getPageResponses(int currentPageNumber, int pageSize,
                                                  ForumSpecsDTO forumSpecsDTO){
        Specification<ForumPost> derivedSpecification = deriveSpecification(forumSpecsDTO);
        //Sort by descending order of creation dates, so the newest posts show up first
        Pageable pageableResult = PageRequest.of(currentPageNumber, pageSize,
                Sort.by("creationDate").descending().
                        //Break any ties by the last modified dates
                        and(Sort.by("lastModifiedDate").descending()));
        Page<ForumPost> resultingPage = postRepository.findAll(derivedSpecification, pageableResult);
        return new ForumPostPageResponse(resultingPage.getContent(), resultingPage.getNumber(),
                resultingPage.getTotalElements(), resultingPage.getTotalPages(), pageSize);
    }


    public PostDisplayDTO loadPost(UUID postID){
        ForumPost forumPost = postRepository.findById(postID).orElseThrow(RuntimeException::new);

        List<CommentDisplayDTO> displayedComments = forumPost.getComments().
                stream().map(this::getCommentInfo).toList();

        return new PostDisplayDTO(forumPost.getTitle(), forumPost.getContent(), forumPost.getAuthorUsername(),
                displayedComments);

    }


    private CommentDisplayDTO getCommentInfo(Comment comment){
        return new CommentDisplayDTO(comment.getId(), comment.getContent(),
                comment.getAuthorUsername(), comment.getRepliesCount());
    }


    //Every minute
    @Scheduled(fixedRate = 60 * 1000)
    public void runLikesUpdatingJob(@Qualifier(value = "updateLikesJob") Job updateLikesJob)
            throws JobInstanceAlreadyCompleteException,
            JobExecutionAlreadyRunningException,
            JobParametersInvalidException, JobRestartException {

        // We don't use a variable to subtract minutes from, because we want to be very
        // sensitive in keeping accurate time. 
        LocalDateTime lastProcessedCutoff = LocalDateTime.now().minusMinutes(10L);

        JobParameters parameters = new JobParametersBuilder()
                .addString("lastProcessed", lastProcessedCutoff.toString())
                .addLong("timestamp", System.currentTimeMillis()).toJobParameters();

        jobLauncher.run(updateLikesJob, parameters);
    }

    /*
    * We use this method to create parameters for each job
    * separately, because we can't use the same timestamp value for the 3 different jobs we want to run.
    * */
    private JobParameters createParameters(Job runningJob)
    {
        return new JobParametersBuilder().
                addLong("timestamp", System.currentTimeMillis()).
                addString("name", runningJob.getName()).toJobParameters();
    }

    //Every 15 seconds
    @Scheduled(fixedRate = 15 * 1000)
    public void runLikesProcessingJobs(@Qualifier(value = "commentLikeJob") Job commentLikeJob,
                                       @Qualifier(value = "postLikeJob") Job postLikeJob,
                                       @Qualifier(value = "commentRepliesLikeJob") Job commentRepliesLikeJob)
            throws JobInstanceAlreadyCompleteException,
            JobExecutionAlreadyRunningException,
            JobParametersInvalidException, JobRestartException {

        jobLauncher.run(commentLikeJob, createParameters(commentLikeJob));
        jobLauncher.run(postLikeJob, createParameters(postLikeJob));
        jobLauncher.run(commentRepliesLikeJob, createParameters(commentRepliesLikeJob));
    }

}

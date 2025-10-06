package com.rebuild.backend.service.forum_services;

import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.entities.forum_entities.Comment;
import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.forms.dtos.forum_dtos.CommentDisplayDTO;
import com.rebuild.backend.model.forms.dtos.forum_dtos.ForumSpecsDTO;
import com.rebuild.backend.model.forms.dtos.forum_dtos.PostDisplayDTO;
import com.rebuild.backend.model.forms.dtos.forum_dtos.SearchResultDTO;
import com.rebuild.backend.model.forms.forum_forms.CommentForm;
import com.rebuild.backend.model.forms.forum_forms.NewPostForm;
import com.rebuild.backend.model.responses.ForumPostPageResponse;
import com.rebuild.backend.repository.CommentRepository;
import com.rebuild.backend.repository.ForumPostRepository;
import com.rebuild.backend.service.resume_services.ResumeService;
import jakarta.persistence.EntityManager;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.job.parameters.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.batch.autoconfigure.JobLauncherApplicationRunner;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ForumPostAndCommentService {


    private final JobOperator jobOperator;
    private final ResumeService resumeService;

    private final CommentRepository commentRepository;

    private final ForumPostRepository postRepository;


    private final EntityManager entityManager;

    private final RedisCacheManager redisCacheManager;


    @Autowired
    public ForumPostAndCommentService(JobOperator jobOperator, ResumeService resumeService,
                                      CommentRepository commentRepository, ForumPostRepository postRepository,
                                      EntityManager entityManager,
                                      @Qualifier("searchCacheManager") RedisCacheManager redisCacheManager) {
        this.jobOperator = jobOperator;
        this.resumeService = resumeService;
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.entityManager = entityManager;
        this.redisCacheManager = redisCacheManager;
    }

    @Transactional
    public ForumPost createNewPost(NewPostForm postForm,
                                   int resumeIndex,
                                   User creatingUser){
        String displayedUsername = determineDisplayedUsername(creatingUser, postForm.remainAnonymous());
        ForumPost newPost = new ForumPost(postForm.title(), postForm.content());
        newPost.setAuthorUsername(displayedUsername);
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
        Comment newComment = new Comment(commentForm.content());
        newComment.setAuthorUsername(displayedUsername);
        newComment.setAssociatedPost(post);
        post.getComments().add(newComment);
        newComment.setParent(null);
        // creatingUser.getMadeComments().add(newComment);
        newComment.setAuthor(creatingUser);
        return commentRepository.save(newComment);

    }

    @Transactional
    public Comment createReplyTo(UUID top_level_comment_id, User creatingUser,
                                      CommentForm commentForm){
        String displayedUsername = determineDisplayedUsername(creatingUser, commentForm.remainAnonymous());
        Comment topLevelComment = commentRepository.findById(top_level_comment_id).
                orElseThrow(RuntimeException::new);

        Comment newComment = new Comment(commentForm.content());
        newComment.setAuthorUsername(displayedUsername);
        newComment.setAuthor(topLevelComment.getAuthor());
        newComment.setAssociatedPost(topLevelComment.getAssociatedPost());
        newComment.setParent(topLevelComment);
        topLevelComment.setRepliesCount(topLevelComment.getRepliesCount() + 1);



        // creatingUser.getMadeReplies().add(newReply);

        return commentRepository.save(newComment);
    }

    private String determineDisplayedUsername(User creatingUser, boolean anonymity){
        return anonymity ? null : creatingUser.getForumUsername();
    }

    public boolean postBelongsToUser(UUID postID, UUID userID){
        return postRepository.countByIdAndUserId(postID, userID) > 0;
    }

    public boolean commentBelongsToUser(UUID commentID, UUID userID){
        return commentRepository.countByIdAndUserId(commentID, userID) > 0;
    }

    @SuppressWarnings(value = "unchecked")
    private SearchResultDTO executeSearch(ForumSpecsDTO forumSpecsDTO, String searchToken){

        if (searchToken == null) {
            SearchSession searchSession = Search.session(entityManager);
            List<UUID> matchedIds = searchSession.search(ForumPost.class)
                    .select(f -> f.id(UUID.class))
                    .where(f -> f.bool().
                            filter(f.match().
                                    fields("title").
                                    matching(forumSpecsDTO.titleContains())).
                            filter(f.match().
                                    fields("content").
                                    matching(forumSpecsDTO.bodyContains())).
                            filter(f.range().
                                    field("creationDate").
                                    atLeast(forumSpecsDTO.postAfterCutoff())).
                            filter(f.range().
                                    field("creationDate").
                                    atMost(forumSpecsDTO.postBeforeCutoff()))

                    )
                    .sort(f -> f.composite(
                            composite -> {
                                composite.add(f.field("creationDate").desc());
                                composite.add(f.field("lastModificationDate").desc());
                            }
                    ))
                    .fetchAllHits();
            String searchResultToken = UUID.randomUUID().toString();
            Objects.requireNonNull(redisCacheManager.getCache("search_cache")).
                    put(searchResultToken, matchedIds);
            return new SearchResultDTO(matchedIds, searchResultToken);

        }
        else {
            return new SearchResultDTO((ArrayList<UUID>) Objects.requireNonNull(
                    Objects.requireNonNull(redisCacheManager.getCache("search_cache")).
                    get(searchToken)).get(), searchToken);
        }
    }

    private List<UUID> getNecessaryResults(List<UUID> allIds, int pageNumber, int pageSize)
    {
        if (allIds == null || allIds.isEmpty()) {
            return List.of();
        }

        int fromIndex = pageNumber * pageSize;
        if (fromIndex >= allIds.size()) {
            return List.of();
        }

        int toIndex = Math.min(fromIndex + pageSize, allIds.size());
        return allIds.subList(fromIndex, toIndex);
    }


    public ForumPostPageResponse getPagedResult(int pageNumber, int pageSize,
                                                        String searchToken, ForumSpecsDTO forumSpecsDTO)
    {
        SearchResultDTO resultDTO = executeSearch(forumSpecsDTO, searchToken);

        List<UUID> matchedResults = resultDTO.results();

        int numPages = Math.ceilDiv(matchedResults.size(), pageSize);

        List<UUID> matchedList = getNecessaryResults(matchedResults, pageNumber, pageSize);

        List<ForumPost> foundPosts = postRepository.findAllById(matchedList);

        return new ForumPostPageResponse(foundPosts, pageNumber,
                matchedResults.size(), numPages, pageSize, resultDTO.searchToken());


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
            JobParametersInvalidException, JobRestartException, NoSuchJobException {


        jobOperator.start(commentLikeJob, createParameters(commentLikeJob));
        jobOperator.start(postLikeJob, createParameters(postLikeJob));
        jobOperator.start(commentRepliesLikeJob, createParameters(commentRepliesLikeJob));
    }

}

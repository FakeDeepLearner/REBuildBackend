package com.rebuild.backend.service.forum_services;

import com.rebuild.backend.model.entities.resume_entities.PostResume;
import com.rebuild.backend.model.entities.forum_entities.PostSearchConfiguration;
import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.entities.forum_entities.Comment;
import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.forms.dtos.forum_dtos.CommentDisplayDTO;
import com.rebuild.backend.model.forms.dtos.forum_dtos.UsernameSearchResultDTO;
import com.rebuild.backend.model.forms.forum_forms.ForumSpecsForm;
import com.rebuild.backend.model.forms.dtos.forum_dtos.PostDisplayDTO;
import com.rebuild.backend.model.forms.dtos.forum_dtos.SearchResultDTO;
import com.rebuild.backend.model.forms.forum_forms.CommentForm;
import com.rebuild.backend.model.forms.forum_forms.NewPostForm;
import com.rebuild.backend.model.responses.ForumPostPageResponse;
import com.rebuild.backend.model.responses.UsernameSearchResponse;
import com.rebuild.backend.repository.forum_repositories.CommentRepository;
import com.rebuild.backend.repository.forum_repositories.ForumPostRepository;
import com.rebuild.backend.repository.forum_repositories.PostSearchRepository;
import com.rebuild.backend.repository.resume_repositories.ResumeRepository;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import com.rebuild.backend.service.util_services.ElasticSearchService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ForumPostAndCommentService {


    private final JobOperator jobOperator;

    private final CommentRepository commentRepository;

    private final ForumPostRepository postRepository;

    private final ResumeRepository resumeRepository;

    private final ElasticSearchService searchService;

    private final PostSearchRepository postSearchRepository;

    private final UserRepository userRepository;


    @Autowired
    public ForumPostAndCommentService(ResumeRepository resumeRepository,
                                      CommentRepository commentRepository, ForumPostRepository postRepository,
                                      JobOperator jobOperator,
                                      ElasticSearchService searchService, PostSearchRepository postSearchRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.resumeRepository = resumeRepository;
        this.jobOperator = jobOperator;
        this.searchService = searchService;
        this.postSearchRepository = postSearchRepository;
        this.userRepository = userRepository;
    }

    public ForumSpecsForm buildSpecsFrom(PostSearchConfiguration configuration)
    {
        return new ForumSpecsForm(configuration.getCreationAfterCutoff().toString(),
                configuration.getCreationBeforeCutoff().toString(),
                configuration.getTitleSearch(), configuration.getBodySearch());
    }

    @Transactional
    public ForumPost createNewPost(NewPostForm postForm,
                                   User creatingUser, List<MultipartFile> resumeFiles){
        ForumPost newPost = new ForumPost(postForm.title(), postForm.content());
        List<PostResume> resumes = resumeRepository.findAllById(postForm.resumeIDs()).stream()
                        .map(PostResume::new).
                        peek(postResume -> postResume.setAssociatedPost(newPost)).
                toList();
        newPost.setResumes(resumes);

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
        ForumPost post = postRepository.findById(post_id).orElseThrow(RuntimeException::new);
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
    public PostSearchConfiguration createSearchConfig(User creatingUser, ForumSpecsForm forumSpecsForm){
        UserProfile profile = creatingUser.getUserProfile();
        PostSearchConfiguration searchConfig = new PostSearchConfiguration(forumSpecsForm);
        searchConfig.setAssociatedProfile(profile);
        profile.addPostSearchConfig(searchConfig);
        return postSearchRepository.save(searchConfig);

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

    private ForumPostPageResponse getPaginatedResponse(int pageNumber, int pageSize, UserProfile profile)
    {
        PageRequest request =
                PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "creationDate"));

        Page<ForumPost> foundPage = postRepository.findAll(request);

        return new ForumPostPageResponse(foundPage.getContent(), foundPage.getNumber(), foundPage.getTotalElements(),
                foundPage.getTotalPages(), foundPage.getSize(), null, profile.getPostSearchConfigurations());
    }

    public ForumPostPageResponse serveGetRequest(int pageNumber, int pageSize, String searchToken, User user)
    {
        UserProfile profile = user.getUserProfile();
        if (searchToken != null)
        {
            SearchResultDTO searchResult = searchService.getFromCache(searchToken);
            if (searchResult != null)
            {
                List<UUID> matchedResults = searchResult.results();

                int numPages = Math.max(1, Math.ceilDiv(matchedResults.size(), pageSize));

                List<UUID> matchedList = searchService.getNecessaryResults(matchedResults, pageNumber, pageSize);

                List<ForumPost> foundPosts = postRepository.findAllById(matchedList);

                return new ForumPostPageResponse(foundPosts, pageNumber,
                        matchedResults.size(), numPages, pageSize, searchResult.searchToken(),
                        profile.getPostSearchConfigurations());
            }
            //Otherwise, we simply return the whole forum post information, paginated.
            else{
                return getPaginatedResponse(pageNumber, pageSize, profile);
            }
        }
        return getPaginatedResponse(pageNumber, pageSize, profile);
    }

    public ForumPostPageResponse getPagedResult(int pageNumber, int pageSize,
                                                ForumSpecsForm forumSpecsForm,
                                                User user)
    {
        UserProfile profile = user.getUserProfile();
        SearchResultDTO resultDTO = searchService.executeSearch(forumSpecsForm);

        List<UUID> matchedResults = resultDTO.results();

        int numPages = Math.max(1, Math.ceilDiv(matchedResults.size(), pageSize));

        List<UUID> matchedList = searchService.getNecessaryResults(matchedResults, pageNumber, pageSize);

        List<ForumPost> foundPosts = postRepository.findAllById(matchedList);

        return new ForumPostPageResponse(foundPosts, pageNumber,
                matchedResults.size(), numPages, pageSize, resultDTO.searchToken(),
                profile.getPostSearchConfigurations());
    }

    public PostDisplayDTO loadPost(UUID postID){
        ForumPost forumPost = postRepository.findById(postID).orElseThrow(RuntimeException::new);

        List<CommentDisplayDTO> displayedComments = postRepository.loadCommentsById(postID);

        return new PostDisplayDTO(forumPost.getTitle(), forumPost.getContent(),
                forumPost.getCreatingUser().getForumUsername(),
                forumPost.getResumes(),
                displayedComments);

    }


    public List<CommentDisplayDTO> getCommentExpansionInfo(UUID parent_id)
    {
        return commentRepository.loadParentCommentInfo(parent_id);
    }


    public UsernameSearchResponse getUsernameSearchResults(String username)
    {
        List<UUID> foundIds = searchService.executeUserSearch(username);

        List<UsernameSearchResultDTO> searchResultDTOS =
                userRepository.findAllById(foundIds).stream()
                        .map(user -> new UsernameSearchResultDTO(user.getId(), user.getForumUsername())).
                        toList();
        return new UsernameSearchResponse(searchResultDTOS);
    }


    /*
     * We use this method to create parameters for each job
     * separately, because we can't use the same timestamp value for the 3 different jobs we want to run.
     * */
    private JobParametersBuilder createParameters(Job runningJob)
    {
        return new JobParametersBuilder().
                addString("name", runningJob.getName());
    }

    //Every minute
    @Scheduled(fixedRate = 60 * 1000)
    public void runLikesUpdatingJob(@Qualifier(value = "updateLikesJob") Job updateLikesJob)
            throws JobInstanceAlreadyCompleteException,
            JobExecutionAlreadyRunningException,
            JobParametersInvalidException, JobRestartException, NoSuchJobException {


        JobParameters parameters = createParameters(updateLikesJob)
                .toJobParameters();

        jobOperator.start(updateLikesJob, parameters);
    }



    //Every 15 seconds
    @Scheduled(fixedRate = 15 * 1000)
    public void runLikesProcessingJobs(@Qualifier(value = "commentLikeJob") Job commentLikeJob,
                                       @Qualifier(value = "postLikeJob") Job postLikeJob)
            throws JobInstanceAlreadyCompleteException,
            JobExecutionAlreadyRunningException,
            JobParametersInvalidException, JobRestartException, NoSuchJobException {


        jobOperator.start(commentLikeJob, createParameters(commentLikeJob).toJobParameters());
        jobOperator.start(postLikeJob, createParameters(postLikeJob).toJobParameters());
    }

}

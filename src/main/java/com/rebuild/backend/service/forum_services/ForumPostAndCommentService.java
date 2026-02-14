package com.rebuild.backend.service.forum_services;

import com.rebuild.backend.batch.BatchJobExecutor;
import com.rebuild.backend.model.entities.forum_entities.*;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.exceptions.BelongingException;
import com.rebuild.backend.model.exceptions.FileUploadException;
import com.rebuild.backend.model.dtos.forum_dtos.CommentDisplayDTO;
import com.rebuild.backend.model.dtos.forum_dtos.UsernameSearchResultDTO;
import com.rebuild.backend.model.forms.forum_forms.ForumSpecsForm;
import com.rebuild.backend.model.dtos.forum_dtos.PostDisplayDTO;
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
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.job.parameters.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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

    private final Dotenv dotenv;

    private final S3AsyncClient s3AsyncClient;

    private final BatchJobExecutor jobRegisterer;


    @Autowired
    public ForumPostAndCommentService(ResumeRepository resumeRepository,
                                      CommentRepository commentRepository, ForumPostRepository postRepository,
                                      JobOperator jobOperator,
                                      ElasticSearchService searchService,
                                      PostSearchRepository postSearchRepository,
                                      UserRepository userRepository, Dotenv dotenv,
                                      S3AsyncClient s3AsyncClient, BatchJobExecutor jobRegisterer) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.resumeRepository = resumeRepository;
        this.jobOperator = jobOperator;
        this.searchService = searchService;
        this.postSearchRepository = postSearchRepository;
        this.userRepository = userRepository;
        this.dotenv = dotenv;
        this.s3AsyncClient = s3AsyncClient;
        this.jobRegisterer = jobRegisterer;
    }

    public ForumSpecsForm buildSpecsFrom(PostSearchConfiguration configuration)
    {
        return new ForumSpecsForm(configuration.getCreationAfterCutoff().toString(),
                configuration.getCreationBeforeCutoff().toString(),
                configuration.getTitleSearch(), configuration.getBodySearch());
    }

    @Transactional
    public ForumPost createNewPost(NewPostForm postForm,
                                   User creatingUser, List<MultipartFile> resumeFiles) {
        ForumPost newPost = new ForumPost(postForm.title(), postForm.content());
        List<PostResume> resumes = resumeRepository.findByUserAndIdIn(creatingUser, postForm.resumeIDs()).stream()
                        .map(PostResume::new).
                        peek(postResume -> postResume.setAssociatedPost(newPost)).
                toList();
        newPost.setResumes(resumes);

        //Calling the upload function in a loop already kick-starts everything for the upload process.
        //This variable is here because we might want to do something with it later just in case.
        List<CompletableFuture<?>> uploadResults = resumeFiles.stream()
                        .map(file -> {
                            byte[] fileBytes;
                            try{
                                fileBytes = file.getBytes();
                            }
                            catch (IOException e) {
                                return CompletableFuture.completedFuture(null);
                            }
                            return uploadFileToS3(file.getOriginalFilename(),
                                    creatingUser, newPost, fileBytes);
                        }).toList();

        //Wait here until all the uploads have been processed.
        CompletableFuture.allOf(uploadResults.toArray(new CompletableFuture[0])).join();

        newPost.setCreatingUser(creatingUser);
        creatingUser.getMadePosts().add(newPost);
        return postRepository.save(newPost);
    }

    private String determineFileKey(User user, UUID randomId, String originalFileName) {
        return user.getId().toString() + "/" + randomId.toString() + "/" + originalFileName;
    }

    private CompletableFuture<Void> uploadFileToS3(String originalFileName,
                                                   User uploadingUser, ForumPost post,
                                                   byte[] fileBytes) {
        String objectKey = determineFileKey(uploadingUser, UUID.randomUUID(), originalFileName);
        String bucketName = dotenv.get("AWS_S3_BUCKET_NAME");

        PutObjectRequest putObjectRequest = PutObjectRequest.builder().
                bucket(bucketName).
                key(objectKey).build();

        AsyncRequestBody requestBody = AsyncRequestBody.fromBytes(fileBytes);


        return s3AsyncClient.putObject(putObjectRequest, requestBody).
                thenAccept(putObjectResponse -> {
                    ResumeFileUploadRecord newUploadRecord = new ResumeFileUploadRecord(bucketName, objectKey,
                            putObjectResponse.expiration(), putObjectResponse.eTag());
                    newUploadRecord.setAssociatedPost(post);

                }).exceptionally(throwable -> {
                    throw new FileUploadException(throwable.getMessage(), throwable);
                });
    }

    private CompletableFuture<Void> deleteFileFromS3(ResumeFileUploadRecord uploadRecord)
    {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(uploadRecord.getBucketName())
                .key(uploadRecord.getObjectKey())
                .ifMatch(uploadRecord.getETag())
                .build();

        return s3AsyncClient.deleteObject(deleteObjectRequest).
                exceptionally(throwable ->  {
                    throw new FileUploadException(throwable.getMessage(), throwable);
                }).thenApply(_ -> null);
    }


    private void deletePostFiles(ForumPost postToDelete)
    {
        List<ResumeFileUploadRecord> fileUploadRecords = postToDelete.getUploadedFiles();

        List<CompletableFuture<Void>> allDeleteResults = fileUploadRecords.stream()
                .map(this::deleteFileFromS3).toList();
    }

    @Transactional
    public void deletePost(UUID postID, User deletingUser){
        ForumPost postToDelete = postRepository.findByIdWithFiles(postID, deletingUser).
                orElseThrow(() -> new BelongingException("This post does not belong to you, so you can't delete it"));

        deletePostFiles(postToDelete);
        //Unlike a create post operation, we do not need to wait for all the uploads to be removed in order
        // to return from the function. When the deletes actually happen is irrelevant in terms of UX.


        postRepository.delete(postToDelete);
    }

    @Transactional
    public void deleteAllPostFiles(User deletingUser){
        List<ForumPost> allUserPosts = postRepository.findByUserWithFiles(deletingUser);

        allUserPosts.forEach(this::deletePostFiles);

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
    public PostSearchConfiguration createSearchConfig(User creatingUser, ForumSpecsForm forumSpecsForm){
        PostSearchConfiguration searchConfig = new PostSearchConfiguration(forumSpecsForm);
        searchConfig.setUser(creatingUser);
        creatingUser.getPostSearchConfigurations().add(searchConfig);
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

    private ForumPostPageResponse getPaginatedResponse(int pageNumber, int pageSize)
    {
        PageRequest request =
                PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "creationDate"));

        Page<ForumPost> foundPage = postRepository.findAll(request);

        return new ForumPostPageResponse(foundPage.getContent(), foundPage.getNumber(), foundPage.getTotalElements(),
                foundPage.getTotalPages(), foundPage.getSize());
    }

    public ForumPostPageResponse serveGetRequest(int pageNumber, int pageSize)
    {
        return getPaginatedResponse(pageNumber, pageSize);
    }

    public ForumPostPageResponse getPagedResult(int pageNumber, int pageSize,
                                                ForumSpecsForm forumSpecsForm)
    {

        List<UUID> matchedResults = searchService.executePostSearch(forumSpecsForm);
        PageRequest request = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "creationDate"));


        Page<ForumPost> foundPosts = postRepository.findByIdIn(matchedResults, request);

        return new ForumPostPageResponse(foundPosts.getContent(), foundPosts.getNumber(),
                foundPosts.getTotalElements(), foundPosts.getTotalPages(), foundPosts.getSize());
    }

    public PostDisplayDTO loadPost(UUID postID){
        ForumPost forumPost = postRepository.findByIdWithMoreInfo(postID).orElseThrow(RuntimeException::new);

        List<CommentDisplayDTO> displayedComments = postRepository.loadCommentsById(postID);

        List<ResumeFileUploadRecord> uploadRecords = forumPost.getUploadedFiles();

        List<String> presignedUrls = uploadRecords.stream().map(resumeFileUploadRecord ->
                createPresignedGetUrl(resumeFileUploadRecord.getBucketName(),
                        resumeFileUploadRecord.getObjectKey())).toList();

        return new PostDisplayDTO(forumPost.getTitle(), forumPost.getContent(),
                forumPost.getCreatingUser().getForumUsername(),
                forumPost.getResumes(),
                displayedComments, presignedUrls);

    }


    /* Create a pre-signed URL to download an object in a subsequent GET request.
    * This is pretty much straight up copied from the AWS code samples */
    private String createPresignedGetUrl(String bucketName, String objectKey) {
        try (S3Presigner presigner = S3Presigner.create()) {

            GetObjectRequest objectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(20))
                    .getObjectRequest(objectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);

            return presignedRequest.url().toExternalForm();
        }
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

    //Every minute
    @Scheduled(fixedRate = 60 * 1000)
    public void runLikesUpdatingJob() throws JobInstanceAlreadyCompleteException, NoSuchJobException,
            JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {

        jobRegisterer.executeJob("updateLikesJob");

    }

    //Every 15 seconds
    @Scheduled(fixedRate = 15 * 1000)
    public void runLikesProcessingJobs()
            throws JobInstanceAlreadyCompleteException,
            JobExecutionAlreadyRunningException,
            JobParametersInvalidException, JobRestartException, NoSuchJobException {

        jobRegisterer.executeJob("postLikeJob");
        jobRegisterer.executeJob("commentLikeJob");


    }

}

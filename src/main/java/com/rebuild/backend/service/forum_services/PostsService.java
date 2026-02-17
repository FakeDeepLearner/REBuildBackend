package com.rebuild.backend.service.forum_services;

import com.rebuild.backend.model.dtos.forum_dtos.CommentDisplayDTO;
import com.rebuild.backend.model.dtos.forum_dtos.PostDisplayDTO;
import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.entities.forum_entities.PostResume;
import com.rebuild.backend.model.entities.forum_entities.PostSearchConfiguration;
import com.rebuild.backend.model.entities.forum_entities.ResumeFileUploadRecord;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.exceptions.BelongingException;
import com.rebuild.backend.model.forms.forum_forms.ForumSpecsForm;
import com.rebuild.backend.model.forms.forum_forms.NewPostForm;
import com.rebuild.backend.repository.forum_repositories.CommentRepository;
import com.rebuild.backend.repository.forum_repositories.ForumPostRepository;
import com.rebuild.backend.repository.forum_repositories.PostSearchRepository;
import com.rebuild.backend.repository.resume_repositories.ResumeRepository;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import com.rebuild.backend.service.util_services.AWSService;
import com.rebuild.backend.service.util_services.ElasticSearchService;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3AsyncClient;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional(readOnly = true)
public class PostsService {


    private final ForumPostRepository postRepository;

    private final ResumeRepository resumeRepository;

    private final PostSearchRepository postSearchRepository;

    private final AWSService awsService;


    @Autowired
    public PostsService(ResumeRepository resumeRepository,
                        ForumPostRepository postRepository,
                        PostSearchRepository postSearchRepository,
                        AWSService awsService) {
        this.postRepository = postRepository;
        this.resumeRepository = resumeRepository;
        this.postSearchRepository = postSearchRepository;
        this.awsService = awsService;
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
                            return awsService.uploadFileToS3(file.getOriginalFilename(),
                                    creatingUser, newPost, fileBytes);
                        }).toList();

        //Wait here until all the uploads have been processed.
        CompletableFuture.allOf(uploadResults.toArray(new CompletableFuture[0])).join();

        newPost.setCreatingUser(creatingUser);
        creatingUser.getMadePosts().add(newPost);
        return postRepository.save(newPost);
    }


    private void deletePostFiles(ForumPost postToDelete)
    {
        List<ResumeFileUploadRecord> fileUploadRecords = postToDelete.getUploadedFiles();

        List<CompletableFuture<Void>> allDeleteResults = fileUploadRecords.stream()
                .map(awsService::deleteFileFromS3).toList();
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
    public PostSearchConfiguration createSearchConfig(User creatingUser, ForumSpecsForm forumSpecsForm,
                                                      boolean isUsedImmediately){
        PostSearchConfiguration searchConfig = new PostSearchConfiguration(forumSpecsForm);
        if (isUsedImmediately)
        {
            searchConfig.setLastUsedTime(Instant.now());
        }
        searchConfig.setUser(creatingUser);
        creatingUser.getPostSearchConfigurations().add(searchConfig);
        return postSearchRepository.save(searchConfig);

    }

    @Transactional
    public void deleteSearchConfig(User user, UUID configId)
    {
        PostSearchConfiguration foundConfig =
                postSearchRepository.findByIdAndUser(configId, user).orElseThrow(
                        () -> new BelongingException("This configuration does not " +
                                "belong to you, so you cannot delete it")
                );
        user.getPostSearchConfigurations().removeIf(
                config -> config.getId().equals(configId));
        postSearchRepository.delete(foundConfig);
    }

    @Transactional
    public PostSearchConfiguration updateSearchConfig(User user, UUID config_id, ForumSpecsForm forumSpecsForm)
    {
        PostSearchConfiguration foundConfig =
                postSearchRepository.findByIdAndUser(config_id, user).orElseThrow(
                        () -> new BelongingException("This configuration does not " +
                                "belong to you, so you cannot update it")
                );
        foundConfig.setBodySearch(forumSpecsForm.bodyContains());
        foundConfig.setTitleSearch(forumSpecsForm.titleContains());
        foundConfig.setCreationAfterCutoff(Instant.parse(forumSpecsForm.postAfterCutoff()));
        foundConfig.setCreationBeforeCutoff(Instant.parse(forumSpecsForm.postBeforeCutoff()));
        foundConfig.setLastUpdatedTime(Instant.now());

        return postSearchRepository.save(foundConfig);
    }

    public PostDisplayDTO loadPost(UUID postID){
        ForumPost forumPost = postRepository.findByIdWithMoreInfo(postID).orElseThrow(RuntimeException::new);

        List<CommentDisplayDTO> displayedComments = postRepository.loadCommentsById(postID);

        List<ResumeFileUploadRecord> uploadRecords = forumPost.getUploadedFiles();

        List<String> presignedUrls = uploadRecords.stream().map(resumeFileUploadRecord ->
                awsService.createPresignedGetUrl(resumeFileUploadRecord.getBucketName(),
                        resumeFileUploadRecord.getObjectKey())).toList();

        return new PostDisplayDTO(forumPost.getTitle(), forumPost.getContent(),
                forumPost.getCreatingUser().getForumUsername(),
                forumPost.getResumes(),
                displayedComments, presignedUrls);

    }

}

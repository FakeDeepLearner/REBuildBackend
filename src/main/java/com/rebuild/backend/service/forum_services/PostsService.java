package com.rebuild.backend.service.forum_services;

import com.rebuild.backend.model.dtos.forum_dtos.CommentDisplayDTO;
import com.rebuild.backend.model.dtos.forum_dtos.PostDisplayDTO;
import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.entities.forum_entities.PostResume;
import com.rebuild.backend.model.entities.forum_entities.ResumeFileUploadRecord;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.exceptions.BelongingException;
import com.rebuild.backend.model.exceptions.FileUploadException;
import com.rebuild.backend.model.forms.forum_forms.NewPostForm;
import com.rebuild.backend.repository.forum_repositories.ForumPostRepository;
import com.rebuild.backend.repository.resume_repositories.ResumeRepository;
import com.rebuild.backend.service.util_services.AWSService;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional(readOnly = true)
public class PostsService {


    private final ForumPostRepository postRepository;

    private final ResumeRepository resumeRepository;

    private final AWSService awsService;


    @Autowired
    public PostsService(ResumeRepository resumeRepository,
                        ForumPostRepository postRepository,
                        AWSService awsService) {
        this.postRepository = postRepository;
        this.resumeRepository = resumeRepository;
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

        List<CompletableFuture<?>> fileUploadResults = new ArrayList<>();
        for (int i = 0; i < resumeFiles.size(); i++)
        {
            MultipartFile file = resumeFiles.get(i);
            try{
                String detectedFileType = new Tika().detect(file.getInputStream());

                if (!"application/pdf".equals(detectedFileType))
                {
                    fileUploadResults.add(CompletableFuture.failedFuture(new FileUploadException(i + 1,
                            "File type is not a pdf", new IllegalArgumentException())));
                }
                else {
                    fileUploadResults.add(awsService.uploadFileToS3(newPost, file.getBytes()));
                }
            }
            catch (IOException e) {
                fileUploadResults.add(CompletableFuture.
                        failedFuture(new FileUploadException(i + 1, e.getMessage(), e.getCause())));
            }
        }
        /*
        List<CompletableFuture<?>> uploadResults = resumeFiles.stream()
                        .map(file -> {

                            try{
                                String detectedFileType = new Tika().detect(file.getInputStream());

                                if (!"application/pdf".equals(detectedFileType))
                                {
                                    return CompletableFuture.failedFuture(new FileUploadException(resumeFiles.indexOf(file),
                                            ))
                                }
                                return awsService.uploadFileToS3(file.getOriginalFilename(),
                                        creatingUser, newPost, file.getBytes());
                            }
                            catch (IOException e) {
                                return CompletableFuture.failedFuture(new FileUploadException(resumeFiles.indexOf(file), e.getMessage(),
                                        e.getCause()));
                            }

                        }).toList();

         */

        //Wait here until all the uploads have been processed.
        CompletableFuture.allOf(fileUploadResults.toArray(new CompletableFuture[0])).join();

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

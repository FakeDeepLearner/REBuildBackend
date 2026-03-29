package com.rebuild.backend.service.forum_services;

import com.rebuild.backend.model.dtos.forum_dtos.CommentDisplayDTO;
import com.rebuild.backend.model.dtos.forum_dtos.PostDisplayDTO;
import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.entities.forum_entities.PostResume;
import com.rebuild.backend.model.entities.forum_entities.ResumeFileUploadRecord;
import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.exceptions.BelongingException;
import com.rebuild.backend.model.exceptions.FileUploadException;
import com.rebuild.backend.model.forms.forum_forms.NewPostForm;
import com.rebuild.backend.repository.forum_repositories.ForumPostRepository;
import com.rebuild.backend.repository.resume_repositories.ResumeRepository;
import com.rebuild.backend.service.util_services.AWSService;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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

    private byte[] sanitizedPDFBytes(byte[] input) throws IOException {
        try(PDDocument originalDocument = Loader.loadPDF(input);
            PDDocument cleanDocument = new PDDocument())
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            for (PDPage page : originalDocument.getPages())
            {
                cleanDocument.importPage(page);
            }
            cleanDocument.save(baos);
            return baos.toByteArray();
        }
    }

    @Transactional
    public ResponseEntity<ForumPost> createNewPost(NewPostForm postForm,
                                                  User creatingUser, List<MultipartFile> resumeFiles) {
        ForumPost newPost = new ForumPost(postForm.title(), postForm.content());
        List<PostResume> resumes = resumeRepository.findByUserAndIdIn(creatingUser, postForm.resumeIDs()).stream()
                        .map(PostResume::new).
                        peek(postResume -> postResume.setAssociatedPost(newPost)).
                toList();
        newPost.setResumes(resumes);

        //First, we sort out potential failures before we even begin uploading the document
        List<byte[]> allSanitizedFileBytes = new ArrayList<>();
        for (MultipartFile file : resumeFiles) {
            String actualFileName = FilenameUtils.getName(file.getOriginalFilename());
            try(InputStream inputStream = file.getInputStream()) {
                String detectedFileType = new Tika().detect(inputStream);
                if (!"application/pdf".equals(detectedFileType)) {
                    throw new FileUploadException("File " + actualFileName + " is not a PDF");
                }
                byte[] sanitizedBytes = sanitizedPDFBytes(file.getBytes());
                allSanitizedFileBytes.add(sanitizedBytes);

            } catch (IOException e) {
                throw new FileUploadException("An error occurred when uploading file " + actualFileName);
            }
        }


        List<CompletableFuture<Void>> allFileUploads = allSanitizedFileBytes.stream()
                .map(input -> awsService.uploadFileToS3(newPost, input)).toList();

        HttpStatus responseStatusCode = HttpStatus.CREATED;
        try {
            //Wait here until all the uploads have been processed.
            // The only possible way for this to fail is if we have something go wrong during the upload itself
            CompletableFuture.allOf(allFileUploads.toArray(new CompletableFuture[0])).get();
        }
        catch (ExecutionException executionException)
        {
            Throwable executionExceptionCause = executionException.getCause();
            if (executionExceptionCause instanceof FileUploadException)
            {

                if (executionExceptionCause.getCause() instanceof IOException _)
                {
                    responseStatusCode = HttpStatus.INTERNAL_SERVER_ERROR;
                }
                if (executionException.getCause() instanceof IllegalArgumentException)
                {
                    responseStatusCode = HttpStatus.BAD_REQUEST;
                }
                return ResponseEntity.status(responseStatusCode).body(postRepository.save(newPost));
            }


        }
        catch(CancellationException | InterruptedException _){

        }

        UserProfile profile = creatingUser.getUserProfile();

        newPost.setAssociatedProfile(profile);
        profile.getMadePosts().add(newPost);
        return ResponseEntity.status(responseStatusCode).body(postRepository.save(newPost));
    }


    private void deletePostFiles(ForumPost postToDelete)
    {
        List<ResumeFileUploadRecord> fileUploadRecords = postToDelete.getUploadedFiles();

        List<CompletableFuture<DeleteObjectResponse>> allDeleteResults = fileUploadRecords.stream()
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
                forumPost.getAssociatedProfile().getUser().getForumUsername(),
                forumPost.getResumes(),
                displayedComments, presignedUrls);

    }

}

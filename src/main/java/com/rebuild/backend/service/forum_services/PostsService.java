package com.rebuild.backend.service.forum_services;

import com.rebuild.backend.model.dtos.forum_dtos.CommentDisplayDTO;
import com.rebuild.backend.model.dtos.forum_dtos.PostDisplayDTO;
import com.rebuild.backend.model.entities.forum_entities.*;
import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.exceptions.BelongingException;
import com.rebuild.backend.model.exceptions.FileUploadException;
import com.rebuild.backend.model.exceptions.NotFoundException;
import com.rebuild.backend.model.forms.forum_forms.NewPostForm;
import com.rebuild.backend.repository.forum_repositories.ForumPostRepository;
import com.rebuild.backend.repository.forum_repositories.LikeRepository;
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
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@Transactional(readOnly = true)
public class PostsService {

    private static final int MAX_FILE_UPLOADS = 5;

    //1 MB = 2 ^ 20 bytes
    private static final long MEGABYTE_IN_BYTES = 1024 * 1024;

    //Each individual file must be 5 MB or less in size
    private static final long FILE_SIZE_LIMIT = 5 * MEGABYTE_IN_BYTES;

    //The files uploaded can have a total size of 15 MB or less
    private static final long TOTAL_FILE_SIZE_LIMIT = 15 * MEGABYTE_IN_BYTES;

    private final ForumPostRepository postRepository;

    private final ResumeRepository resumeRepository;

    private final AWSService awsService;

    private final LikeRepository likeRepository;


    @Autowired
    public PostsService(ResumeRepository resumeRepository,
                        ForumPostRepository postRepository,
                        AWSService awsService, LikeRepository likeRepository) {
        this.postRepository = postRepository;
        this.resumeRepository = resumeRepository;
        this.awsService = awsService;
        this.likeRepository = likeRepository;
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

    private List<byte[]> doFilePreprocessing(List<MultipartFile> resumeFiles)
    {
        List<byte[]> result = new ArrayList<>();
        if (resumeFiles.size() > MAX_FILE_UPLOADS)
        {
            throw new FileUploadException(HttpStatus.BAD_REQUEST, "You cannot upload more than " + MAX_FILE_UPLOADS + " files");
        }
        long totalFileSize = resumeFiles.stream().mapToLong(MultipartFile::getSize).sum();

        if (totalFileSize > TOTAL_FILE_SIZE_LIMIT)
        {
            throw new FileUploadException(HttpStatus.BAD_REQUEST, "The total size of the files is too large");
        }

        for (MultipartFile file : resumeFiles)
        {
            String actualFileName = FilenameUtils.getName(file.getOriginalFilename());
            if (file.getSize() >= FILE_SIZE_LIMIT)
            {
                throw new FileUploadException(HttpStatus.BAD_REQUEST, "File " + actualFileName + " is too large");
            }
            try(InputStream inputStream = file.getInputStream())
            {
                String detectedFileType = new Tika().detect(inputStream);
                if (!"application/pdf".equals(detectedFileType)) {
                    throw new FileUploadException(HttpStatus.BAD_REQUEST, "File " + actualFileName + " is not a PDF");
                }
                byte[] sanitizedBytes = sanitizedPDFBytes(file.getBytes());
                result.add(sanitizedBytes);
            }
            catch (IOException _)
            {
                throw new FileUploadException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "An error occurred while uploading file " + actualFileName);
            }
        }
        return result;
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

        /*
        This function takes care of all possible errors that can arise early,
         so we never begin the upload process if we don't have to. If everything is ok with the files,
         it returns the byte arrays of all files in a sanitized (i.e. stripped from its metadata) manner
        */
        List<byte[]> allSanitizedFileBytes = doFilePreprocessing(resumeFiles);

        List<CompletableFuture<Void>> allFileUploads = allSanitizedFileBytes.stream()
                .map(input -> awsService.uploadFileToS3(newPost, input)).toList();

        try {
            //Wait here until all the uploads have been processed.
            // The only possible way for this to fail is if we have something go wrong during the upload itself
            CompletableFuture.allOf(allFileUploads.toArray(new CompletableFuture[0])).get();
        }
        catch (Exception _){
            throw new FileUploadException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "An unexpected error occurred");

        }

        UserProfile profile = creatingUser.getUserProfile();

        newPost.setAssociatedProfile(profile);
        profile.getMadePosts().add(newPost);
        return postRepository.save(newPost);
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

    public PostDisplayDTO loadPost(UUID postID, User loadingUser){
        ForumPost forumPost = postRepository.findByIdWithMoreInfo(postID).orElseThrow(
                () -> new NotFoundException("Post with this id is not found")
        );

        List<CommentDisplayDTO> displayedComments = postRepository.loadCommentsById(postID, loadingUser.getId());

        List<ResumeFileUploadRecord> uploadRecords = forumPost.getUploadedFiles();

        List<String> presignedUrls = uploadRecords.stream().map(resumeFileUploadRecord ->
                awsService.createPresignedGetUrl(resumeFileUploadRecord.getBucketName(),
                        resumeFileUploadRecord.getObjectKey())).toList();

        return new PostDisplayDTO(forumPost.getTitle(), forumPost.getContent(),
                forumPost.getAssociatedProfile().getUser().getForumUsername(),
                forumPost.getResumes(),
                displayedComments, presignedUrls,
                likeRepository.findByLikedObjectIdAndLikingUserId(postID, loadingUser.getId()).isPresent());

    }


    public ForumPost likePost(UUID comment_id, User likingUser)
    {
        ForumPost post = postRepository.findById(comment_id).orElseThrow(
                () -> new NotFoundException("Post with this id is not found"));

        Optional<Like> foundLike = likeRepository.findByLikedObjectIdAndLikingUserId(comment_id,
                likingUser.getId());

        //If the user has already liked this comment, remove the like.
        foundLike.ifPresent(like -> {
            likeRepository.delete(like);
            post.setLikeCount(post.getLikeCount() - 1);
        });

        //If the user has not like this comment, simply add a like for this comment for this user.

        Like newLike = new Like(likingUser.getId(), comment_id);

        likeRepository.save(newLike);
        post.setLikeCount(post.getLikeCount() + 1);

        return postRepository.save(post);
    }

}

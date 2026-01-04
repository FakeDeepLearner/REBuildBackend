package com.rebuild.backend.controllers.forum_controllers;

import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.forms.forum_forms.NewPostForm;
import com.rebuild.backend.service.forum_services.ForumPostAndCommentService;
import com.rebuild.backend.service.resume_services.ResumeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.util.List;
import java.util.PriorityQueue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("api/forum/posts")
@Transactional
public class PostController {

    private final ResumeService resumeService;

    private final ForumPostAndCommentService forumPostAndCommentService;

    @Autowired
    public PostController(ResumeService resumeService, ForumPostAndCommentService forumPostAndCommentService) {
        this.resumeService = resumeService;
        this.forumPostAndCommentService = forumPostAndCommentService;
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ForumPost createNewPost(@Valid @RequestPart(name = "form") NewPostForm postForm,
                                   @RequestPart(name = "files") List<MultipartFile> resumeFiles,
                                   @AuthenticationPrincipal User creatingUser) {
        return forumPostAndCommentService.createNewPost(postForm,
                creatingUser, resumeFiles);
    }

    @DeleteMapping("/delete/{post_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@PathVariable UUID post_id,
                           @AuthenticationPrincipal User creatingUser) {
        forumPostAndCommentService.deletePost(post_id, creatingUser);
    }
}

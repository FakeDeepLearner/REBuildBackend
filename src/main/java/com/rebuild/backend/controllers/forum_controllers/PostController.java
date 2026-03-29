package com.rebuild.backend.controllers.forum_controllers;

import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.forms.forum_forms.NewPostForm;
import com.rebuild.backend.service.forum_services.PostsService;
import com.rebuild.backend.service.resume_services.ResumeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/forum/posts")
@Transactional
public class PostController {

    private final ResumeService resumeService;

    private final PostsService postsService;

    @Autowired
    public PostController(ResumeService resumeService, PostsService postsService) {
        this.resumeService = resumeService;
        this.postsService = postsService;
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ForumPost> createNewPost(@Valid @RequestPart(name = "form") NewPostForm postForm,
                                        @RequestPart(name = "files") List<MultipartFile> resumeFiles,
                                        @AuthenticationPrincipal User creatingUser) {
        return postsService.createNewPost(postForm,
                creatingUser, resumeFiles);
    }

    @DeleteMapping("/delete/{post_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@PathVariable UUID post_id,
                           @AuthenticationPrincipal User creatingUser) {
        postsService.deletePost(post_id, creatingUser);
    }
}

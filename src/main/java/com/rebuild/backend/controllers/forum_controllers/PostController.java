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
import org.springframework.security.core.userdetails.UserDetails;
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

    private final ForumPostAndCommentService forumPostAndCommentService;

    @Autowired
    public PostController(ResumeService resumeService, ForumPostAndCommentService forumPostAndCommentService) {
        this.resumeService = resumeService;
        this.forumPostAndCommentService = forumPostAndCommentService;
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ForumPost createNewPost(@Valid @RequestPart(name = "metadata") NewPostForm postForm,
                                   @RequestPart(name = "file") List<MultipartFile> resumeFiles,
                                   @AuthenticationPrincipal User creatingUser) {
        return forumPostAndCommentService.createNewPost(postForm,
                creatingUser, resumeFiles);
    }

    @DeleteMapping("/delete/{post_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@PathVariable UUID post_id,
                           @AuthenticationPrincipal User creatingUser) {
        if(!forumPostAndCommentService.postBelongsToUser(post_id, creatingUser.getId())) {
            throw new RuntimeException("That post does not belong to you");
        }
        forumPostAndCommentService.deletePost(post_id);
    }
}

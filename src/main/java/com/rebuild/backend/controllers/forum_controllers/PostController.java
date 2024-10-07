package com.rebuild.backend.controllers.forum_controllers;

import com.rebuild.backend.exceptions.forum_exceptions.ResumeForbiddenException;
import com.rebuild.backend.model.entities.User;
import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.forms.forum_forms.NewPostForm;
import com.rebuild.backend.service.ForumService;
import com.rebuild.backend.service.user_services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
@Transactional
public class PostController {

    private final UserService userService;

    private final ForumService forumService;

    @Autowired
    public PostController(UserService userService, ForumService forumService) {
        this.userService = userService;
        this.forumService = forumService;
    }

    @PostMapping("/create/{resume_id}")
    public ForumPost createNewPost(@PathVariable UUID resume_id,
                                   @Valid @RequestBody NewPostForm postForm,
                                   @AuthenticationPrincipal UserDetails creatingDetails) {
        User actualUser = (User) creatingDetails;
        Resume creatingResume = actualUser.getResumes().stream().filter(resume -> resume.getId().equals(resume_id)).
                findFirst().orElse(null);
        //This can't happen normally, the main purpose of this is to protect against malicious attacks.
        if (creatingResume == null) {
            throw new ResumeForbiddenException("That resume does not belong to you");
        }
        return forumService.createNewPost(postForm.title(), postForm.content(), creatingResume, actualUser);
    }
}

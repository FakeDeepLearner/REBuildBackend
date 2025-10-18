package com.rebuild.backend.controllers.forum_controllers;

import com.rebuild.backend.model.entities.forum_entities.PostSearchConfiguration;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.forms.forum_forms.ForumSpecsForm;
import com.rebuild.backend.model.forms.dtos.forum_dtos.PostDisplayDTO;
import com.rebuild.backend.model.responses.ForumPostPageResponse;
import com.rebuild.backend.repository.forum_repositories.PostSearchRepository;
import com.rebuild.backend.service.forum_services.ForumPostAndCommentService;
import com.rebuild.backend.service.user_services.UserService;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

import static org.springframework.http.HttpStatus.CONFLICT;

@RestController
@RequestMapping("/api/forum")
public class ForumHomePageController {

    private final ForumPostAndCommentService postAndCommentService;

    private final UserService userService;

    private final PostSearchRepository postSearchRepository;

    @Autowired
    public ForumHomePageController(ForumPostAndCommentService postAndCommentService,
                                   UserService userService, PostSearchRepository postSearchRepository) {
        this.postAndCommentService = postAndCommentService;
        this.userService = userService;
        this.postSearchRepository = postSearchRepository;
    }

    @PostMapping( "/create_post_search_config")
    @ResponseStatus(HttpStatus.CREATED)
    public PostSearchConfiguration createSearchConfig(@AuthenticationPrincipal User authenticatedUser,
                                                      @RequestBody ForumSpecsForm specsForm)
    {
        return postAndCommentService.createSearchConfig(authenticatedUser, specsForm);
    }

    @PostMapping("/get_posts/configuration/{config_id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getPosts(@AuthenticationPrincipal User user,
                                           @PathVariable UUID config_id) {
        try {

            PostSearchConfiguration foundConfig = postSearchRepository.findById(config_id).get();

            ForumSpecsForm craftedBody = postAndCommentService.buildSpecsFrom(foundConfig);

            ForumPostPageResponse response =
                    postAndCommentService.getPagedResult(0, 20,
                             craftedBody, user);

            return ResponseEntity.ok(response);
        }
        catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/get_posts")
    @ResponseStatus(HttpStatus.OK)
    public ForumPostPageResponse getPosts(@RequestParam(defaultValue = "0", name = "page")
                                          int pageNumber,

                                          @RequestParam(defaultValue = "20", name = "size")
                                          int pageSize,

                                          @RequestBody ForumSpecsForm forumSpecsForm,
                                          @AuthenticationPrincipal User user) {

        return postAndCommentService.getPagedResult(pageNumber, pageSize, forumSpecsForm, user);
    }

    @GetMapping("/get_posts")
    @ResponseStatus(HttpStatus.OK)
    public ForumPostPageResponse getPosts(@RequestParam(defaultValue = "0", name = "page") int pageNumber,
                                          @RequestParam(defaultValue = "20", name = "size") int pageSize,
                                          @RequestParam(name = "token", required = false)  String searchToken,
                                          @AuthenticationPrincipal User user) {
        return postAndCommentService.serveGetRequest(pageNumber, pageSize, searchToken, user);
    }

    @GetMapping("/get_posts/{post_id}")
    @ResponseStatus(HttpStatus.OK)
    public PostDisplayDTO loadPost(@PathVariable UUID post_id) {
        return postAndCommentService.loadPost(post_id);
    }

    @PostMapping("/change_username")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> changeUsername(@AuthenticationPrincipal User authenticatedUser,
                                            @RequestBody String newUsername) {
        try {
            User changedUser = userService.modifyForumUsername(authenticatedUser, newUsername);
            return ResponseEntity.ok(changedUser);
        } catch (DataIntegrityViolationException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ConstraintViolationException violationException) {
                if (Objects.equals(violationException.getConstraintName(), "uk_forum_username")) {
                    return ResponseEntity.status(CONFLICT).body("This username is taken");
                }
            }

        }
        return null;
    }
}

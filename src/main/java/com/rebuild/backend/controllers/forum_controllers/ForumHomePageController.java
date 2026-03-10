package com.rebuild.backend.controllers.forum_controllers;

import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.dtos.StatusAndError;
import com.rebuild.backend.model.forms.forum_forms.ForumSpecsForm;
import com.rebuild.backend.model.dtos.forum_dtos.PostDisplayDTO;
import com.rebuild.backend.model.responses.ForumPostPageResponse;
import com.rebuild.backend.model.responses.UsernameSearchResponse;
import com.rebuild.backend.service.forum_services.ForumHomePageService;
import com.rebuild.backend.service.forum_services.PostsService;
import com.rebuild.backend.service.forum_services.FriendshipService;
import com.rebuild.backend.service.user_services.UserService;
import lombok.NonNull;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.UUID;


@RestController
@RequestMapping("/api/forum")
public class ForumHomePageController {

    private final PostsService postAndCommentService;

    private final UserService userService;

    private final FriendshipService friendshipService;

    private final ForumHomePageService homePageService;

    @Autowired
    public ForumHomePageController(PostsService postAndCommentService,
                                   UserService userService, FriendshipService friendshipService, ForumHomePageService homePageService) {
        this.postAndCommentService = postAndCommentService;
        this.userService = userService;
        this.friendshipService = friendshipService;
        this.homePageService = homePageService;
    }

    @PostMapping("/username_search")
    public UsernameSearchResponse searchUsernames(@RequestBody String username)
    {
        return homePageService.getUsernameSearchResults(username);
    }

    @PostMapping("/get_posts")
    @ResponseStatus(HttpStatus.OK)
    public ForumPostPageResponse loadForumWithSpecsForm(@RequestParam(defaultValue = "0", name = "page", required = false)
                                          int pageNumber,

                                          @RequestParam(defaultValue = "20", name = "size", required = false)
                                          int pageSize,

                                          @RequestBody ForumSpecsForm forumSpecsForm) {

        return homePageService.getPagedResult(pageNumber, pageSize, forumSpecsForm);
    }

    @GetMapping("/get_posts")
    @ResponseStatus(HttpStatus.OK)
    public ForumPostPageResponse getPosts(@RequestParam(defaultValue = "0", name = "page", required = false) int pageNumber,
                                          @RequestParam(defaultValue = "20", name = "size", required = false) int pageSize) {
        return homePageService.serveGetRequest(pageNumber, pageSize);
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
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("This username is taken");
                }
            }

        }
        return null;
    }


    @PostMapping("/accept_request/{request_id}")
    public ResponseEntity<@NonNull String> acceptFriendshipRequest(@PathVariable UUID request_id,
                                                                   @AuthenticationPrincipal User acceptingUser) {
        StatusAndError result = friendshipService.acceptFriendshipRequest(acceptingUser, request_id);
        return ResponseEntity.ok(result.message());

    }

    @PostMapping("/decline_request/{request_id}")
    public ResponseEntity<@NonNull String> declineFriendshipRequest(@PathVariable UUID request_id,
                                                                    @AuthenticationPrincipal User acceptingUser) {
        StatusAndError result =
                friendshipService.declineFriendshipRequest(acceptingUser, request_id);

        return ResponseEntity.status(result.status()).body(result.message());

    }

    @PostMapping("/send_friendship/{recipient_id}")
    public ResponseEntity<@NonNull String> sendFriendshipRequest(@PathVariable UUID recipient_id,
                                                                 @AuthenticationPrincipal User sendingUser)
    {
        StatusAndError requestResult =
                friendshipService.sendFriendRequest(sendingUser, recipient_id);

        return ResponseEntity.status(requestResult.status()).body(requestResult.message());
    }
}

package com.rebuild.backend.controllers.forum_controllers;

import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.forms.forum_forms.ForumSpecsForm;
import com.rebuild.backend.model.dtos.forum_dtos.comment_and_post_dtos.PostDisplayDTO;
import com.rebuild.backend.model.responses.forum_responses.ForumPostPageResponse;
import com.rebuild.backend.model.responses.user_responses.UsernameSearchResponse;
import com.rebuild.backend.service.forum_services.ForumHomePageService;
import com.rebuild.backend.service.forum_services.PostsService;
import com.rebuild.backend.service.forum_services.FriendshipService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/forum")
public class ForumHomePageController {

    private final PostsService postAndCommentService;

    private final FriendshipService friendshipService;

    private final ForumHomePageService homePageService;

    @Autowired
    public ForumHomePageController(PostsService postAndCommentService,
                                   FriendshipService friendshipService, ForumHomePageService homePageService) {
        this.postAndCommentService = postAndCommentService;
        this.friendshipService = friendshipService;
        this.homePageService = homePageService;
    }

    @PostMapping("/username_search")
    public UsernameSearchResponse searchUsernames(@RequestBody String username,
                                                  @AuthenticationPrincipal User user,
                                                  @RequestParam(defaultValue = "0", name = "page", required = false)
                                                      int pageNumber)
    {
        return homePageService.getUsernameSearchResults(username, user, pageNumber);
    }

    @PostMapping("/get_posts")
    @ResponseStatus(HttpStatus.OK)
    public ForumPostPageResponse loadForumWithSpecsForm(@RequestParam(defaultValue = "0", name = "page", required = false)
                                          int pageNumber,

                                          @RequestBody ForumSpecsForm forumSpecsForm,
                                          @AuthenticationPrincipal User user) {

        return homePageService.loadForumWithSpecsForm(pageNumber, forumSpecsForm, user);
    }

    @GetMapping("/get_posts")
    @ResponseStatus(HttpStatus.OK)
    public ForumPostPageResponse getPosts(@RequestParam(defaultValue = "0", name = "page", required = false) int pageNumber,
                                          @AuthenticationPrincipal User user) {
        return homePageService.loadForum(pageNumber, user);
    }

    @GetMapping("/get_posts/{post_id}")
    @ResponseStatus(HttpStatus.OK)
    public PostDisplayDTO loadPost(@PathVariable UUID post_id, @AuthenticationPrincipal User user,
                                   @RequestParam(defaultValue = "30", name = "size", required = false) int pageSize) {
        return postAndCommentService.loadPost(post_id, user, pageSize);
    }

    @PostMapping("/accept_request/{request_id}")
    public ResponseEntity<@NonNull String> acceptFriendshipRequest(@PathVariable UUID request_id,
                                                                   @AuthenticationPrincipal User acceptingUser) {
        String result = friendshipService.acceptFriendshipRequest(acceptingUser, request_id);
        return ResponseEntity.ok("You have added " + result + " as a friend");

    }

    @PostMapping("/decline_request/{request_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void declineFriendshipRequest(@PathVariable UUID request_id,
                                                                    @AuthenticationPrincipal User acceptingUser) {
        friendshipService.declineFriendshipRequest(acceptingUser, request_id);
    }

    @PostMapping("/send_friendship/{recipient_id}")
    public ResponseEntity<@NonNull String> sendFriendshipRequest(@PathVariable UUID recipient_id,
                                                                 @AuthenticationPrincipal User sendingUser)
    {

        friendshipService.sendFriendRequest(sendingUser, recipient_id);

        return ResponseEntity.ok("Friendship request sent");
    }
}

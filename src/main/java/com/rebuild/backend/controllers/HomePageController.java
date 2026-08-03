package com.rebuild.backend.controllers;

import com.rebuild.backend.model.dtos.user_dtos.FriendRequestDTO;
import com.rebuild.backend.model.dtos.user_dtos.UserFriendDTO;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.responses.user_responses.FriendRequestResponse;
import com.rebuild.backend.service.forum_services.FriendshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/home")
public class HomePageController {

    private final FriendshipService friendshipService;

    @Autowired
    public HomePageController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    @GetMapping("/friends/received_requests")
    @ResponseStatus(HttpStatus.OK)
    public List<FriendRequestResponse> loadReceivedFriendRequests(@AuthenticationPrincipal User authenticatedUser) {
        return friendshipService.loadReceivedFriendRequests(authenticatedUser);
    }

    @GetMapping("/friends/sent_requests")
    @ResponseStatus(HttpStatus.OK)
    public List<FriendRequestResponse> loadSentFriendRequests(@AuthenticationPrincipal User authenticatedUser) {
        return friendshipService.loadSentFriendRequests(authenticatedUser);
    }

    @GetMapping("/friends")
    @ResponseStatus(HttpStatus.OK)
    public List<UserFriendDTO> loadFriends(@AuthenticationPrincipal User authenticatedUser) {
        return friendshipService.getUserFriends(authenticatedUser);
    }


}

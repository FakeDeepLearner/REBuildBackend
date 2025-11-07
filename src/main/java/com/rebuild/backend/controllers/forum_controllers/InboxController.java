package com.rebuild.backend.controllers.forum_controllers;

import com.rebuild.backend.model.entities.messaging_and_friendship_entities.RequestStatus;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.FriendRequest;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.forms.dtos.StatusAndError;
import com.rebuild.backend.model.forms.dtos.forum_dtos.FriendRequestDTO;
import com.rebuild.backend.repository.forum_repositories.FriendRelationshipRepository;
import com.rebuild.backend.repository.forum_repositories.FriendRequestRepository;
import com.rebuild.backend.service.forum_services.FriendAndMessageService;
import com.rebuild.backend.service.user_services.UserService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inbox")
public class InboxController {

    private final FriendAndMessageService friendAndMessageService;
    private final UserService userService;

    private final FriendRequestRepository friendRequestRepository;

    @Autowired
    public InboxController(FriendAndMessageService friendAndMessageService, UserService userService,
                           FriendRequestRepository friendRequestRepository) {
        this.friendAndMessageService = friendAndMessageService;
        this.userService = userService;
        this.friendRequestRepository = friendRequestRepository;
    }

    @PostMapping("/accept_request/{request_id}")
    public ResponseEntity<@NonNull String> acceptFriendshipRequest(@PathVariable UUID request_id,
                                                                   @AuthenticationPrincipal User acceptingUser) {
        StatusAndError result = friendAndMessageService.addFriend(acceptingUser, request_id);

        return ResponseEntity.ok(result.message());

    }

    @PostMapping("/decline_request/{request_id}")
    public ResponseEntity<@NonNull String> declineFriendshipRequest(@PathVariable UUID request_id,
                                                                   @AuthenticationPrincipal User acceptingUser) {
        StatusAndError result =
                friendAndMessageService.declineFriendshipRequest(acceptingUser, request_id);

        return ResponseEntity.status(result.status()).body(result.message());

    }

    @PostMapping("/send_friendship/{recipient_id}")
    public ResponseEntity<@NonNull String> sendFriendshipRequest(@PathVariable UUID recipient_id,
                                                                 @AuthenticationPrincipal User sendingUser)
    {
        StatusAndError requestResult =
                friendAndMessageService.sendFriendRequest(sendingUser, recipient_id);

        return ResponseEntity.status(requestResult.status()).body(requestResult.message());
    }
}

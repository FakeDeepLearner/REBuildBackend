package com.rebuild.backend.controllers.forum_controllers;

import com.rebuild.backend.model.entities.enums.RequestStatus;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.FriendRequest;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.repository.FriendRelationshipRepository;
import com.rebuild.backend.repository.FriendRequestRepository;
import com.rebuild.backend.service.forum_services.FriendAndMessageService;
import com.rebuild.backend.service.user_services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

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

    //TODO: This one method has 3 database transactions. See if there is a way to reduce that
    @PostMapping("/accept_request/{request_index}")
    public void acceptFriendshipRequest(@PathVariable int request_index,
                                        @AuthenticationPrincipal User acceptingUser) {
        List<FriendRequest> userRequests = acceptingUser.getInbox().getPendingRequests();
        FriendRequest requestToAccept =
                userRequests.get(request_index);

        requestToAccept.setStatus(RequestStatus.ACCEPTED);
        requestToAccept.setStatusUpdateDate(LocalDateTime.now());
        friendRequestRepository.save(requestToAccept);
        friendAndMessageService.addFriend(requestToAccept.getSender(), requestToAccept.getRecipient());


        //We remove this friend request from the recipient's inbox. It is accepted, there is no point of
        //it staying there anymore.
        userRequests.remove(request_index);
        userService.save(acceptingUser);

    }

    @PostMapping("/send_request")
    public void sendFriendshipRequest(@RequestBody String recipientUsername,
                                      @AuthenticationPrincipal User sendingUser)
    {


    }
}

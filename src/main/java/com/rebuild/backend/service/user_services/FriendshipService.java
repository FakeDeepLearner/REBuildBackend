package com.rebuild.backend.service.user_services;

import com.rebuild.backend.model.dtos.user_dtos.FriendRequestDTO;
import com.rebuild.backend.model.dtos.user_dtos.UserFriendDTO;
import com.rebuild.backend.model.dtos.websocket_dtos.friendship_dtos.FriendRequestActionDTO;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.*;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.responses.user_responses.FriendRequestResponse;
import com.rebuild.backend.service.util_services.WebsocketsService;
import com.rebuild.backend.utils.UserPair;
import com.rebuild.backend.utils.exceptions.BelongingException;
import com.rebuild.backend.utils.exceptions.FriendshipException;
import com.rebuild.backend.utils.exceptions.NotFoundException;
import com.rebuild.backend.repository.messaging_and_friendship_repositories.FriendshipRepository;
import com.rebuild.backend.repository.messaging_and_friendship_repositories.FriendRequestRepository;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class FriendshipService {

    private final WebsocketsService websocketsService;

    private final UserRepository userRepository;

    private final FriendshipRepository friendshipRepository;

    private final FriendRequestRepository friendRequestRepository;

    @Autowired
    public FriendshipService(WebsocketsService websocketsService, UserRepository userRepository,
                             FriendshipRepository friendshipRepository,
                             FriendRequestRepository friendRequestRepository) {
        this.websocketsService = websocketsService;
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.friendRequestRepository = friendRequestRepository;
    }

    private Friendship makeUsersFriends(User receiver, FriendRequest acceptedRequest)
    {
        User sender = acceptedRequest.getSender();

        UserPair userPair = new UserPair(sender, receiver);

        Friendship newRelationship = new Friendship(userPair);

        return friendshipRepository.save(newRelationship);
    }
    
    public void acceptFriendshipRequest(User receiver, UUID friendRequestId)
    {
        FriendRequest friendRequest = friendRequestRepository.findByIdAndRecipient(friendRequestId, receiver).
                orElseThrow(() ->
                        new BelongingException("This friend request either does not " +
                                "exist or has not been addressed to you."));

        User sender = friendRequest.getSender();

        Friendship _ = makeUsersFriends(receiver, friendRequest);

        friendRequestRepository.delete(friendRequest);

        FriendRequestActionDTO requestActionDTO = new FriendRequestActionDTO(receiver.getForumUsername(),
                true);

        websocketsService.sendFriendActionNotification(requestActionDTO, sender.getForumUsername());
    }

    public void acceptAllFriendshipRequests(User recipient)
    {
        List<FriendRequest> requests = friendRequestRepository.findByRecipient(recipient);

        List<Friendship> newFriendships = requests.stream().map(request ->
        {
            Friendship newFriendship = makeUsersFriends(recipient, request);
            User sender = request.getSender();
            FriendRequestActionDTO requestActionDTO = new FriendRequestActionDTO(recipient.getForumUsername(),
                    true);

            websocketsService.sendFriendActionNotification(requestActionDTO, sender.getForumUsername());
            return newFriendship;
        }).toList();

        friendRequestRepository.deleteAll(requests);

        friendshipRepository.saveAll(newFriendships);
    }
    
    public void declineFriendshipRequest(User declininguser, UUID friendRequestId)
    {
        FriendRequest friendRequest = friendRequestRepository.findByIdAndRecipient(friendRequestId, declininguser).
                orElseThrow(() ->
                        new BelongingException("This friend request either does not exist or has not been addressed to you."));

        FriendRequestActionDTO requestActionDTO = new FriendRequestActionDTO(declininguser.getForumUsername(),
                false);

        websocketsService.sendFriendActionNotification(requestActionDTO, friendRequest.getSender().getForumUsername());
        friendRequestRepository.delete(friendRequest);
    }

    public void declineAllFriendshipRequests(User recipient)
    {
        List<FriendRequest> requests = friendRequestRepository.findByRecipient(recipient);

        requests.forEach(friendRequest -> {
            User sender =  friendRequest.getSender();

            FriendRequestActionDTO requestActionDTO = new FriendRequestActionDTO(recipient.getForumUsername(),
                    false);

            websocketsService.sendFriendActionNotification(requestActionDTO, sender.getForumUsername());
        });

        friendRequestRepository.deleteAll(requests);

    }

    public void cancelFriendshipRequest(User cancellingUser, UUID friendRequestId)
    {
        FriendRequest friendRequest = friendRequestRepository.findByIdAndSender(friendRequestId,
                cancellingUser).orElseThrow(() -> new BelongingException("This friend request either " +
                "does not exist or has not been sent by you"));

        friendRequestRepository.delete(friendRequest);
    }
    
    public void sendFriendRequest(User sender, UUID recipientId, String requestContent)
    {
        if (recipientId.equals(sender.getId()))
        {
            throw new FriendshipException(HttpStatus.FORBIDDEN, "You can't send friend requests to yourself.");
        }
        User recipient = userRepository.findById(recipientId).orElseThrow(() ->
                new NotFoundException("User with the given id not found"));

        UserPair userPair = new UserPair(recipient, sender);

        boolean foundRequest =
                friendRequestRepository.existsBySenderAndRecipient(sender, recipient);

        if (!foundRequest) {
            throw new FriendshipException(HttpStatus.CONFLICT,
                    "You already have an existing friend request with this user, you cannot send " +
                            "another one while it is active.");
        }

        boolean foundRelationship =
                friendshipRepository.existsByLowUserIdAndHighUserId(userPair.lowId(), userPair.highId());

        if (!foundRelationship) {
            throw new FriendshipException(HttpStatus.CONFLICT,
                    "You are already friends with this user.");
        }

        FriendRequest newRequest = new FriendRequest(sender, recipient, requestContent);

        FriendRequest savedRequest = friendRequestRepository.save(newRequest);

        websocketsService.sendFriendRequestNotification(savedRequest);
    }

    public List<FriendRequestResponse> loadReceivedFriendRequests(User loadingUser)
    {
        return friendRequestRepository.loadReceivedRequestsByUser(loadingUser).stream().map(
                FriendRequestDTO::convertToResponse
        ).toList();
    }

    public List<FriendRequestResponse> loadSentFriendRequests(User loadingUser)
    {
        return friendRequestRepository.loadSentRequestsByUser(loadingUser).stream().map(
                FriendRequestDTO::convertToResponse
        ).toList();
    }

    public List<UserFriendDTO> getUserFriends(User loadingUser)
    {
        return friendshipRepository.findFriendshipsById(loadingUser.getId());
    }


}

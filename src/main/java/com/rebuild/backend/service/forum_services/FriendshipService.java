package com.rebuild.backend.service.forum_services;

import com.rebuild.backend.model.entities.messaging_and_friendship_entities.*;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.utils.UserPair;
import com.rebuild.backend.utils.exceptions.BelongingException;
import com.rebuild.backend.model.dtos.user_dtos.UsernameSearchResultDTO;
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
import java.util.Optional;
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

    
    public String acceptFriendshipRequest(User receiver, UUID friendRequestId)
    {
        FriendRequest friendRequest = friendRequestRepository.findByIdAndRecipient(friendRequestId, receiver).
                orElseThrow(() ->
                        new BelongingException("This friend request either does not exist or has not been addressed to you."));


        User sender = friendRequest.getSender();

        UserPair userPair = new UserPair(sender, receiver);

        Friendship newRelationship = new Friendship(userPair);

        friendRequestRepository.delete(friendRequest);

        friendshipRepository.save(newRelationship);

        return sender.getForumUsername();
    }

    
    public void declineFriendshipRequest(User declininguser, UUID friendRequestId)
    {
        FriendRequest friendRequest = friendRequestRepository.findByIdAndRecipient(friendRequestId, declininguser).
                orElseThrow(() ->
                        new BelongingException("This friend either does not exist or has not been addressed to you."));

        friendRequestRepository.delete(friendRequest);
    }


    
    public void sendFriendRequest(User sender, UUID recipientId)
    {
        User recipient = userRepository.findById(recipientId).orElseThrow(() ->
                new NotFoundException("User with the given id not found"));

        UserPair userPair = new UserPair(recipient, sender);

        Optional<FriendRequest> foundRequest =
                friendRequestRepository.findByLowUserIdAndHighUserId(userPair.lowId(), userPair.highId());

        if (foundRequest.isPresent()) {
            throw new FriendshipException(HttpStatus.CONFLICT,
                    "You already have an existing friend request with this user, you cannot send " +
                            "another one until it is either declined or times out.");
        }

        Optional<Friendship> foundRelationship =
                friendshipRepository.findByLowUserIdAndHighUserId(userPair.lowId(), userPair.highId());

        if (foundRelationship.isPresent()) {
            throw new FriendshipException(HttpStatus.CONFLICT,
                    "You are already friends with this user");
        }

        FriendRequest newRequest = new FriendRequest(sender, recipient);

        FriendRequest savedRequest = friendRequestRepository.save(newRequest);

        websocketsService.sendFriendRequestNotification(savedRequest);
    }

    public List<UsernameSearchResultDTO> loadUserFriendRequests(User loadingUser)
    {
        return friendRequestRepository.loadByUser(loadingUser);
    }


}

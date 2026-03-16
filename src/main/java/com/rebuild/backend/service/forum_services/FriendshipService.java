package com.rebuild.backend.service.forum_services;

import com.rebuild.backend.model.entities.messaging_and_friendship_entities.*;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.exceptions.BelongingException;
import com.rebuild.backend.model.dtos.StatusAndError;
import com.rebuild.backend.model.dtos.forum_dtos.FriendRequestDTO;
import com.rebuild.backend.model.dtos.forum_dtos.UsernameSearchResultDTO;
import com.rebuild.backend.repository.forum_repositories.FriendRelationshipRepository;
import com.rebuild.backend.repository.forum_repositories.FriendRequestRepository;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import com.rebuild.backend.service.util_services.RabbitMQService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FriendshipService {

    private final UserRepository userRepository;

    private final FriendRelationshipRepository friendRelationshipRepository;

    private final FriendRequestRepository friendRequestRepository;

    private final RabbitMQService rabbitMQService;

    @Autowired
    public FriendshipService(UserRepository userRepository,
                             FriendRelationshipRepository friendRelationshipRepository,
                             FriendRequestRepository friendRequestRepository,
                             RabbitMQService rabbitMQService) {
        this.userRepository = userRepository;
        this.friendRelationshipRepository = friendRelationshipRepository;
        this.friendRequestRepository = friendRequestRepository;
        this.rabbitMQService = rabbitMQService;
    }

    @Transactional
    public StatusAndError acceptFriendshipRequest(User receiver, UUID friendRequestId)
    {
        FriendRequest friendRequest = friendRequestRepository.findByIdAndRecipient(friendRequestId, receiver).
                orElseThrow(() ->
                        new BelongingException("This friend request either does not exist or has not been addressed to you."));


        User sender = friendRequest.getSender();

        FriendRelationship newRelationship = new FriendRelationship(sender, receiver);

        friendRelationshipRepository.save(newRelationship);

        friendRequestRepository.delete(friendRequest);

        return new StatusAndError(HttpStatus.OK, "You have added " + sender.getForumUsername() + " as a friend");

    }

    @Transactional
    public void declineFriendshipRequest(User declininguser, UUID friendRequestId)
    {
        FriendRequest friendRequest = friendRequestRepository.findByIdAndRecipient(friendRequestId, declininguser).
                orElseThrow(() ->
                        new BelongingException("This friend either does not exist or has not been addressed to you."));

        friendRequestRepository.delete(friendRequest);
    }


    @Transactional
    public StatusAndError sendFriendRequest(User sender, UUID recipientId)
    {
        User recipient = userRepository.findById(recipientId).orElse(null);

        assert recipient != null : "Recipient not found";

        Optional<FriendRequest> foundRequest =
                friendRequestRepository.findByTwoUsers(sender, recipient);

        if (foundRequest.isPresent()) {
            return new StatusAndError(HttpStatus.CONFLICT,
                    "You already have an existing friend request with this user, you cannot send " +
                            "another one until it is either declined or times out.");
        }

        Optional<FriendRelationship> foundRelationship =
                friendRelationshipRepository.findByTwoUsers(sender, recipient);
        if (foundRelationship.isPresent()) {
            return new StatusAndError(HttpStatus.CONFLICT,
                    "You are already friends with this user");
        }

        FriendRequestDTO friendRequestDTO = new FriendRequestDTO(sender, recipientId);

        rabbitMQService.sendFriendshipRequest(friendRequestDTO);
        return new StatusAndError(HttpStatus.OK, "The request has been sent");
    }

    public List<UsernameSearchResultDTO> loadUserFriendRequests(User loadingUser)
    {
        return friendRequestRepository.loadByUser(loadingUser);
    }


}

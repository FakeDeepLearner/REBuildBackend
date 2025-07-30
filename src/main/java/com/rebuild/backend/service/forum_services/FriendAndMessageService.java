package com.rebuild.backend.service.forum_services;

import com.rebuild.backend.model.entities.messaging_and_friendship_entities.FriendRelationship;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.repository.FriendRelationshipRepository;
import com.rebuild.backend.repository.UserRepository;
import com.rebuild.backend.service.user_services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FriendAndMessageService {

    private final UserRepository userRepository;
    private final FriendRelationshipRepository friendRelationshipRepository;

    @Autowired
    public FriendAndMessageService(UserRepository userRepository,
                                   FriendRelationshipRepository friendRelationshipRepository) {
        this.userRepository = userRepository;
        this.friendRelationshipRepository = friendRelationshipRepository;
    }

    //Friendship is symmetric, so it doesn't matter for this method who the users are
    public void addFriend(User sender, User recipient)
    {
        FriendRelationship friendRelationship = new FriendRelationship(sender, recipient);

        friendRelationshipRepository.save(friendRelationship);
    }


    public void sendFriendRequest(User sender, String recipientUsername)
    {
        
    }

}

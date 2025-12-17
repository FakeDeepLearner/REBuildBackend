package com.rebuild.backend.batch.processors;

import com.rebuild.backend.model.entities.messaging_and_friendship_entities.FriendRequest;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.forms.dtos.forum_dtos.FriendRequestDTO;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FriendsProcessor implements ItemProcessor<FriendRequestDTO, FriendRequest> {

    private final UserRepository userRepository;

    @Autowired
    public FriendsProcessor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public FriendRequest process(FriendRequestDTO item) throws Exception {
        User foundUser = userRepository.findById(item.recipientId()).orElse(null);

        if(foundUser == null) {
            return null;
        }

        FriendRequest newRequest = new FriendRequest(item.sender(), foundUser, foundUser.getInbox());
        foundUser.getInbox().addFriendRequest(newRequest);

        return newRequest;
    }
}

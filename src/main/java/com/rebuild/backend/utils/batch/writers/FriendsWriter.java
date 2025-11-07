package com.rebuild.backend.utils.batch.writers;

import com.rebuild.backend.model.entities.messaging_and_friendship_entities.FriendRequest;
import com.rebuild.backend.repository.forum_repositories.FriendRequestRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class FriendsWriter implements ItemWriter<FriendRequest> {

    private final FriendRequestRepository friendRequestRepository;

    public FriendsWriter(FriendRequestRepository friendRequestRepository) {
        this.friendRequestRepository = friendRequestRepository;
    }

    @Override
    public void write(Chunk<? extends FriendRequest> chunk) throws Exception {
        friendRequestRepository.saveAll(friendRequestRepository.findAll());
    }
}

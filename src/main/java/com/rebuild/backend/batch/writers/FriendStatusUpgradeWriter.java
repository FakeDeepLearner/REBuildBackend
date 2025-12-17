package com.rebuild.backend.batch.writers;

import com.rebuild.backend.model.entities.messaging_and_friendship_entities.FriendRequest;
import com.rebuild.backend.repository.forum_repositories.FriendRequestRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FriendStatusUpgradeWriter implements ItemWriter<FriendRequest> {

    private final FriendRequestRepository friendRequestRepository;

    @Autowired
    public FriendStatusUpgradeWriter(FriendRequestRepository friendRequestRepository) {
        this.friendRequestRepository = friendRequestRepository;
    }

    @Override
    public void write(Chunk<? extends FriendRequest> chunk) {

        List<FriendRequest> requestList = new ArrayList<>(chunk.getItems());

        friendRequestRepository.deleteAllInBatch(requestList);
    }
}

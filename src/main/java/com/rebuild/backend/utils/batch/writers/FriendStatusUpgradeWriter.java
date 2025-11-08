package com.rebuild.backend.utils.batch.writers;

import com.rebuild.backend.model.entities.messaging_and_friendship_entities.FriendRequest;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.RequestStatus;
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
    public void write(Chunk<? extends FriendRequest> chunk) throws Exception {
        List<FriendRequest> requestList = new ArrayList<>();

        for (FriendRequest friendRequest : chunk.getItems()) {
            friendRequest.setStatus(RequestStatus.TIMED_OUT);
            requestList.add(friendRequest);
        }

        friendRequestRepository.saveAll(requestList);
    }
}

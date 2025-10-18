package com.rebuild.backend.repository.forum_repositories;

import com.rebuild.backend.model.entities.messaging_and_friendship_entities.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, UUID> {
}

package com.rebuild.backend.repository.forum_repositories;

import com.rebuild.backend.model.entities.messaging_and_friendship_entities.FriendRelationship;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.FriendRequest;
import com.rebuild.backend.model.entities.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, UUID> {

    @Query(
            value = "SELECT r from FriendRequest r " +
                    "WHERE r.status='PENDING' AND ((r.recipient=?1 AND r.sender=?2)" +
                    "OR (r.recipient=?2 AND r.sender=?1))"
    )
    Optional<FriendRequest> findByTwoUsers(User user1, User user2);
}

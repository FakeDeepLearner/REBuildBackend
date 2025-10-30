package com.rebuild.backend.repository.forum_repositories;

import com.rebuild.backend.model.entities.messaging_and_friendship_entities.Chat;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.FriendRelationship;
import com.rebuild.backend.model.entities.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FriendRelationshipRepository extends JpaRepository<FriendRelationship, UUID> {

    @Query(
            value = "SELECT r from FriendRelationship r " +
                    "WHERE (r.recipient=?1 AND r.sender=?2)" +
                    "OR (r.recipient=?2 AND r.sender=?1)"
    )
    Optional<FriendRelationship> findByTwoUsers(User user1, User user2);
}

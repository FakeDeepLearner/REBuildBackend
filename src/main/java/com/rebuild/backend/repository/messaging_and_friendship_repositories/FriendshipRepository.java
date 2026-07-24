package com.rebuild.backend.repository.messaging_and_friendship_repositories;

import com.rebuild.backend.model.entities.messaging_and_friendship_entities.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {

    Optional<Friendship> findByLowUserIdAndHighUserId(UUID lowUserId, UUID highUserId);

}

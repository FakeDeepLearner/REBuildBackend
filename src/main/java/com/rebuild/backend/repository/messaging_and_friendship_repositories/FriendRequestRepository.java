package com.rebuild.backend.repository.messaging_and_friendship_repositories;

import com.rebuild.backend.model.dtos.user_dtos.FriendRequestDTO;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.FriendRequest;
import com.rebuild.backend.model.entities.user_entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, UUID> {


    boolean existsByLowUserIdAndHighUserId(UUID lowUserId, UUID highUserId);

    @Query(value = """
    SELECT f FROM FriendRequest f
    JOIN FETCH f.sender
    WHERE f.id=?1 AND f.recipient=?2
    """)
    Optional<FriendRequest> findByIdAndRecipient(UUID id, User recipient);

    Optional<FriendRequest> findByIdAndSender(UUID id, User sender);

    @Query(value = """
            SELECT NEW com.rebuild.backend.model.dtos.user_dtos.FriendRequestDTO(
            f.id, s.forumUsername, f.creationTimestamp)
            FROM FriendRequest f
            JOIN f.sender s
            WHERE f.recipient=?1 ORDER BY f.creationTimestamp DESC
           """)
    List<FriendRequestDTO> loadReceivedRequestsByUser(User user);


    @Query(value = """
            SELECT NEW com.rebuild.backend.model.dtos.user_dtos.FriendRequestDTO(
            f.id, s.forumUsername, f.creationTimestamp)
            FROM FriendRequest f
            JOIN f.recipient s
            WHERE f.sender=?1 ORDER BY f.creationTimestamp DESC
           """)
    List<FriendRequestDTO> loadSentRequestsByUser(User user);
}

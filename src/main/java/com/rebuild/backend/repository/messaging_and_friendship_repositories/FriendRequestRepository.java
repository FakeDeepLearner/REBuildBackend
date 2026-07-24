package com.rebuild.backend.repository.messaging_and_friendship_repositories;

import com.rebuild.backend.model.entities.messaging_and_friendship_entities.FriendRequest;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.dtos.user_dtos.UsernameSearchResultDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, UUID> {


    Optional<FriendRequest> findByLowUserIdAndHighUserId(UUID lowUserId, UUID highUserId);

    Optional<FriendRequest> findByIdAndRecipient(UUID id, User recipient);


    @Query(value = """
            SELECT NEW com.rebuild.backend.model.dtos.user_dtos.FriendRequestDTO(
            f.id, s.forumUsername, f.creationTimestamp)
            FROM FriendRequest f
            JOIN f.sender s
            WHERE f.recipient=?1
           """)
    List<UsernameSearchResultDTO> loadByUser(User user);
}

package com.rebuild.backend.repository.messaging_and_friendship_repositories;

import com.rebuild.backend.model.dtos.user_dtos.UserFriendDTO;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {

    boolean existsByLowUserIdAndHighUserId(UUID lowUserId, UUID highUserId);

    @Query(value = """
        SELECT NEW com.rebuild.backend.model.dtos.user_dtos.UserFriendDTO(u.id, u.forumUsername,
                u.imageUrl)
        FROM Friendship f JOIN User u
        ON u.id = CASE WHEN f.lowUserId=?1 THEN f.highUserId ELSE f.lowUserId END
        WHERE f.highUserId=?1 OR f.lowUserId=?1
        """
    )
    List<UserFriendDTO> findFriendshipsById(UUID id);

}

package com.rebuild.backend.repository.forum_repositories;

import com.rebuild.backend.model.entities.messaging_and_friendship_entities.Chat;
import com.rebuild.backend.model.entities.users.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatRepository extends JpaRepository<Chat, UUID> {

    @Query(
          value = "SELECT c from Chat c " +
                  "WHERE c.initiatingUser=?1 OR c.receivingUser=?1"
    )
    List<Chat> findByUser(User user);


    @Query(
            value = "SELECT c from Chat c " +
                    "WHERE (c.initiatingUser=?1 AND c.receivingUser=?2)" +
                    "OR (c.initiatingUser=?2 AND c.receivingUser=?1)"
    )
    Optional<Chat> findByTwoUsers(User user1, User user2);

}

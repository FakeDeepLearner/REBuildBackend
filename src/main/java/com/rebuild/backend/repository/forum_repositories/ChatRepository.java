package com.rebuild.backend.repository.forum_repositories;

import com.rebuild.backend.model.entities.messaging_and_friendship_entities.Chat;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.Message;
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

    @Query(value = """
            SELECT c FROM Chat c
            JOIN FETCH c.initiatingUser JOIN FETCH c.receivingUser
            WHERE c.initiatingUser=?1 OR c.receivingUser=?1
        """
    )
    List<Chat> findByUser(User user);


    @Query(
            value = "SELECT c from Chat c " +
                    "WHERE (c.initiatingUser=?1 AND c.receivingUser=?2)" +
                    "OR (c.initiatingUser=?2 AND c.receivingUser=?1)"
    )
    Optional<Chat> findByTwoUsers(User user1, User user2);


    @Query(value = """
    SELECT c FROM Chat c
    LEFT JOIN FETCH c.messages m JOIN FETCH m.sender
    WHERE c.id=:chatId""")
    Optional<Chat> findByIdWithMessages(UUID chatId);

}

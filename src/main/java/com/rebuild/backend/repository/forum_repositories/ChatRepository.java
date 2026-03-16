package com.rebuild.backend.repository.forum_repositories;

import com.rebuild.backend.model.entities.messaging_and_friendship_entities.Chat;
import com.rebuild.backend.model.entities.user_entities.User;
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
    LEFT JOIN FETCH c.messages m JOIN FETCH m.sender
    WHERE c.id=:chatId""")
    Optional<Chat> findByIdWithMessages(UUID chatId);

}

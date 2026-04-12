package com.rebuild.backend.repository.forum_repositories;

import com.rebuild.backend.model.entities.messaging_and_friendship_entities.chat_entities.AbstractChat;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.chat_entities.PrivateChat;
import com.rebuild.backend.model.entities.user_entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatRepository extends JpaRepository<AbstractChat, UUID> {

    @Query(value = """
    SELECT c FROM AbstractChat c
    LEFT JOIN FETCH c.messages m JOIN FETCH m.sender
    WHERE c.id=:chatId""")
    Optional<AbstractChat> findByIdWithMessages(UUID chatId);



    @Query("""
    SELECT c FROM PrivateChat c WHERE
    c.sender=:user OR c.recipient=:user
    """)
    List<PrivateChat> findPrivateChatsByUser(User user);

    @Query("""
    SELECT c From PrivateChat c
    WHERE (c.sender=?1 AND c.recipient=?2) OR (c.sender=?2 AND c.recipient=?1)
    """)
    Optional<PrivateChat> findPrivateChatsByUsers(User user1, User user2);


}

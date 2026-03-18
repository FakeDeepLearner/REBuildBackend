package com.rebuild.backend.repository.forum_repositories;

import com.rebuild.backend.model.entities.messaging_and_friendship_entities.AbstractChat;
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
    SELECT c FROM AbstractChat c
    WHERE
        (TYPE(c) = PrivateChat AND\s
        (TREAT(c AS PrivateChat).sender=:user OR TREAT(c AS PrivateChat).recipient=:user))
        OR
        (TYPE(c) = GroupChat AND EXISTS (
            SELECT 1 FROM ChatParticipation p
            WHERE p.participatedChat = TREAT(c as GroupChat) AND p.participatingUser=:user
            ORDER BY p.unreadMessagesCount DESC
        ))
""")
    List<AbstractChat> findAllChatsForUser(User user);

}

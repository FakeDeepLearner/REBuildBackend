package com.rebuild.backend.repository.messaging_and_friendship_repositories;

import com.rebuild.backend.model.entities.util_entitites.base_entities.AbstractChat;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.PrivateChat;
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
    WHERE c.id=?1""")
    Optional<AbstractChat> findByIdWithMessages(UUID chatId);

    @Query(value = """
    SELECT c FROM AbstractChat c
    LEFT JOIN FETCH c.participations
    WHERE c.id=?1
    """)
    Optional<AbstractChat> findByIdWithParticipations(UUID chatId);


    @Query("""
    SELECT cp.participatedChat.id FROM ChatParticipation cp
    WHERE cp.isMuted=false AND cp.participatingUser=?1
    """)
    List<UUID> findIdsByUser(User user);

}

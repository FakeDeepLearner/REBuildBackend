package com.rebuild.backend.repository.messaging_and_friendship_repositories;

import com.rebuild.backend.model.entities.messaging_and_friendship_entities.Message;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.entities.util_entitites.base_entities.AbstractChat;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    Optional<Message> findByIdAndSender(UUID id, User sender);

    Slice<Message> findByAssociatedChat(AbstractChat associatedChat, Pageable pageable);

    Slice<Message> findByAssociatedChat_Id(UUID associatedChatId, Pageable pageable);

    @Query(nativeQuery = true,
    value = """
    SELECT m FROM messages m
    WHERE m.associated_chat_id=?1 AND m.is_deleted=false
    AND to_tsvector(m.content) @@ websearch_to_tsquery('english', ?2)
    """)
    Slice<Message> findByChatAndSimilarContent(UUID chatId, String query, Pageable pageable);

    Optional<Message> findByIdAndAssociatedChat_Id(UUID id, UUID associatedChatId);
    
    @Query(value = """
    SELECT m FROM Message m
    WHERE m.associatedChat.id=?1 AND m.isPinned=true
    """)
    Slice<Message> findPinnedMessagesByChatId(UUID chatId, Pageable pageable);


}

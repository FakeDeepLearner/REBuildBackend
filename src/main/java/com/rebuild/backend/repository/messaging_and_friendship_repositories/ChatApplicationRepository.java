package com.rebuild.backend.repository.messaging_and_friendship_repositories;

import com.rebuild.backend.model.dtos.user_dtos.ChatApplicationFetchDTO;
import com.rebuild.backend.model.entities.chat_entities.GroupChat;
import com.rebuild.backend.model.entities.chat_entities.JoinChatApplication;
import com.rebuild.backend.model.entities.user_entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatApplicationRepository extends JpaRepository<JoinChatApplication, UUID> {

    boolean existsByAssociatedChatAndAssociatedUser(GroupChat associatedChat, User associatedUser);

    @Query(value = """
    SELECT ca FROM JoinChatApplication ca
    JOIN FETCH ca.associatedUser u
    WHERE ca.associatedChat=?1
    """)
    List<JoinChatApplication> findByAssociatedChat(GroupChat associatedChat);
    
    List<JoinChatApplication> findByAssociatedUser(User associatedUser);

    Optional<JoinChatApplication> findByIdAndAssociatedChat(UUID id, GroupChat associatedChat);

    @Query(value = """
    SELECT NEW com.rebuild.backend.model.dtos.user_dtos.ChatApplicationFetchDTO(ca.id, c.chatName,
        ca.content, ca.createdAt)
    FROM JoinChatApplication ca JOIN ca.associatedChat c
    WHERE ca.associatedUser=?1
    """)
    Slice<ChatApplicationFetchDTO> findByAssociatedUser(User associatedUser, Pageable pageable);

    @Query(value = """
    SELECT NEW com.rebuild.backend.model.dtos.user_dtos.ChatApplicationFetchDTO(ca.id, u.forumUsername,
        ca.content, ca.createdAt)
    FROM JoinChatApplication ca JOIN ca.associatedUser u
    WHERE ca.associatedChat=?1
    """)
    Slice<ChatApplicationFetchDTO> findByAssociatedChat(GroupChat groupChat, Pageable pageable);

    boolean existsByIdAndAssociatedUser(UUID id, User associatedUser);

}

package com.rebuild.backend.repository.chat_repositories;

import com.rebuild.backend.model.entities.chat_entities.ChatParticipation;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.entities.util_entitites.AbstractChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatParticipationRepository extends JpaRepository<ChatParticipation, UUID> {

    @Query(value = """
    SELECT cp FROM ChatParticipation cp
    JOIN cp.associatedChat ch
    WHERE TYPE(ch) = PrivateChat AND cp.participatingUser=?1
    """)
    List<ChatParticipation> findPrivateParticipationsParticipatingUser(User participatingUser);

    @Query(value = """
    SELECT cp FROM ChatParticipation cp
    JOIN cp.associatedChat ch
    WHERE TYPE(ch) = GroupChat AND cp.participatingUser=?1
    """)
    List<ChatParticipation> findGroupParticipationsParticipatingUser(User participatingUser);


    Optional<ChatParticipation> findByParticipatingUserAndAssociatedChat(User participatingUser,
                                                                         AbstractChat participatedChat);

    boolean existsByAssociatedChatAndParticipatingUser(AbstractChat participatedChat, User participatingUser);

    Optional<ChatParticipation> findByParticipatingUser_IdAndAssociatedChat_Id(UUID participatingUserId,
                                                                               UUID participatedChatId);

    boolean existsByAssociatedChat_IdAndParticipatingUser(UUID participatedChatId, User participatingUser);

    @Query(value = """
    SELECT cp FROM ChatParticipation cp
    JOIN FETCH cp.associatedChat ch
    WHERE ch.id=?1 AND cp.participatingUser=?2
    """)
    Optional<ChatParticipation> findByChatIdAndUser(UUID chatId,
                                                    User user);


    @Query(value = """
    SELECT cp FROM ChatParticipation cp
    JOIN FETCH cp.associatedChat ch
    JOIN FETCH ch.participations
    WHERE ch.id=?1 AND cp.participatingUser=?2
    """)
    Optional<ChatParticipation> findByChatIdAndUserWithParticipations(UUID chatId,
                                                                      User user);

    @Query("""
    SELECT cp.associatedChat.id FROM ChatParticipation cp
    WHERE cp.isMuted=false AND cp.participatingUser=?1
    """)
    List<UUID> findIdsByUser(User user);

}

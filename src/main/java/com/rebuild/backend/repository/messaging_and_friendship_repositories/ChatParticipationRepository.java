package com.rebuild.backend.repository.messaging_and_friendship_repositories;

import com.rebuild.backend.model.entities.messaging_and_friendship_entities.ChatParticipation;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.entities.util_entitites.base_entities.AbstractChat;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatParticipationRepository extends JpaRepository<ChatParticipation, UUID> {

    List<ChatParticipation> findByParticipatingUser(User participatingUser);


    Optional<ChatParticipation> findByParticipatingUserAndParticipatedChat(User participatingUser,
                                                                           AbstractChat participatedChat);

    Optional<ChatParticipation> findByParticipatingUser_IdAndParticipatedChat_Id(UUID participatingUserId,
                                                                                 UUID participatedChatId);
    boolean existsByParticipatedChat_IdAndParticipatingUser(UUID participatedChatId, User participatingUser);

    @Query(value = """
    SELECT cp FROM ChatParticipation cp
    JOIN FETCH cp.participatedChat ch
    WHERE ch.id=?1 AND cp.participatingUser=?2
    """)
    Optional<ChatParticipation> findByChatIdAndUser(UUID chatId,
                                                    User user);


    @Query(value = """
    SELECT cp FROM ChatParticipation cp
    JOIN FETCH cp.participatedChat ch
    JOIN FETCH ch.participations
    WHERE ch.id=?1 AND cp.participatingUser=?2
    """)
    Optional<ChatParticipation> findByChatIdAndUserWithParticipations(UUID chatId,
                                                                      User user);

}

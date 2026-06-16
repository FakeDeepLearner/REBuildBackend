package com.rebuild.backend.repository.messaging_and_friendship_repositories;

import com.rebuild.backend.model.entities.messaging_and_friendship_entities.ChatParticipation;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.entities.util_entitites.base_entities.AbstractChat;
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

    Optional<ChatParticipation> findByParticipatingUser_IdAndParticipatedChat(UUID participatingUserId,
                                                                              AbstractChat participatedChat);

    boolean existsByParticipatedChat_IdAndParticipatingUser(UUID participatedChatId, User participatingUser);
}

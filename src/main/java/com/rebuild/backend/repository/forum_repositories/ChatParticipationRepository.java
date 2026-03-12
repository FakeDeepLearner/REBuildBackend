package com.rebuild.backend.repository.forum_repositories;

import com.rebuild.backend.model.entities.messaging_and_friendship_entities.ChatParticipation;
import com.rebuild.backend.model.entities.user_entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ChatParticipationRepository extends JpaRepository<ChatParticipation, UUID> {

    @Query(value = """
        SELECT p FROM ChatParticipation p
        JOIN FETCH p.participatedChat c
        JOIN FETCH c.participations cp
        JOIN FETCH cp.participatingUser
        WHERE p.participatingUser=:user
       """)
    List<ChatParticipation> findParticipationsByUser(User user);
}

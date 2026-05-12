package com.rebuild.backend.repository.messaging_and_friendship_repositories;

import com.rebuild.backend.model.entities.messaging_and_friendship_entities.ChatParticipation;
import com.rebuild.backend.model.entities.user_entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatParticipationRepository extends JpaRepository<ChatParticipation, UUID> {

    List<ChatParticipation> findByParticipatingUser(User participatingUser);

    @Query(value = """
        SELECT c.id FROM ChatParticipation cp
        JOIN cp.participatedChat c
        WHERE cp.participatingUser=?1
        """)
    List<UUID> findIdsByParticipatingUser(User participatingUser);
}

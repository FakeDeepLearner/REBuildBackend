package com.rebuild.backend.repository.messaging_and_friendship_repositories;

import com.rebuild.backend.model.entities.chat_entities.ChatInvitation;
import com.rebuild.backend.model.entities.user_entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatInvitationRepository extends JpaRepository<ChatInvitation, UUID> {


    Optional<ChatInvitation> findBySenderAndRecipientAndAssociatedChat_Id(User sender,
                                                                          User recipient,
                                                                          UUID associatedChatId);


    Optional<ChatInvitation> findByIdAndRecipient(UUID id, User recipient);

    @Query(value = """
    SELECT ci FROM ChatInvitation ci
    JOIN FETCH ci.associatedChat c
    WHERE ci.recipient=?1
    """)
    List<ChatInvitation> findByRecipient(User recipient);
}

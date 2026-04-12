package com.rebuild.backend.repository.forum_repositories;

import com.rebuild.backend.model.entities.messaging_and_friendship_entities.chat_entities.ChatInvitation;
import com.rebuild.backend.model.entities.user_entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatInvitationRepository extends JpaRepository<ChatInvitation, UUID> {


    Optional<ChatInvitation> findBySenderAndRecipientAndAssociatedChat_Id(User sender,
                                                                          User recipient,
                                                                          UUID associatedChatId);


    Optional<ChatInvitation> findByIdAndRecipient(UUID id, User recipient);
}

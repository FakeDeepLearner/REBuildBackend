package com.rebuild.backend.repository.messaging_and_friendship_repositories;

import com.rebuild.backend.model.entities.chat_entities.PrivateChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PrivateChatRepository extends JpaRepository<PrivateChat, UUID> {

    Optional<PrivateChat> findByLowUserIdAndHighUserId(UUID lowUserId, UUID highUserId);
}

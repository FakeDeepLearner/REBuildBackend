package com.rebuild.backend.repository.messaging_and_friendship_repositories;

import com.rebuild.backend.model.entities.chat_entities.GroupChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GroupChatRepository extends JpaRepository<GroupChat, UUID> {


}

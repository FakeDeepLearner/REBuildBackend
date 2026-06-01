package com.rebuild.backend.controllers.forum_controllers;

import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.repository.messaging_and_friendship_repositories.ChatParticipationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
public class TypingNotificationsController {

    private final ChatParticipationRepository chatParticipationRepository;

    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public TypingNotificationsController(ChatParticipationRepository chatParticipationRepository,
                                         SimpMessagingTemplate simpMessagingTemplate) {
        this.chatParticipationRepository = chatParticipationRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @MessageMapping("/typing-notification/{chatId}")
    public void sendTypingNotification(@DestinationVariable UUID chatId,
                                       @AuthenticationPrincipal User sender){
        boolean userIsInChat = chatParticipationRepository.existsByParticipatedChat_IdAndParticipatingUser(chatId, sender);

        // If the user is not actually in this chat, then we don't do anything. If we don't have this check,
        // a malicious user could send "typing" notifications to any chat that they want.

        if (!userIsInChat){
            return;
        }

        simpMessagingTemplate.convertAndSend(
                "/typing/" + chatId,
                sender.getForumUsername()
        );

    }
}

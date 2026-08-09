package com.rebuild.backend.service.util_services;

import com.rebuild.backend.model.dtos.websocket_dtos.chat_dtos.ChatInvitationNotificationDTO;
import com.rebuild.backend.model.dtos.websocket_dtos.chat_dtos.KickedNotificationDTO;
import com.rebuild.backend.model.dtos.websocket_dtos.chat_dtos.NewChatNotificationDTO;
import com.rebuild.backend.model.dtos.websocket_dtos.chat_dtos.NewMessageNotificationDTO;
import com.rebuild.backend.model.dtos.websocket_dtos.friendship_dtos.FriendRequestActionDTO;
import com.rebuild.backend.model.dtos.websocket_dtos.friendship_dtos.FriendRequestNotificationDTO;
import com.rebuild.backend.model.entities.chat_entities.ChatInvitation;
import com.rebuild.backend.model.entities.chat_entities.ChatParticipation;
import com.rebuild.backend.model.entities.chat_entities.GroupChat;
import com.rebuild.backend.model.entities.chat_entities.Message;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.*;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.entities.util_entitites.AbstractChat;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebsocketsService {

    private final SimpMessagingTemplate simpMessagingTemplate;

    public WebsocketsService(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }


    private String determineContentPreview(String fullContent)
    {
        //If the length of the message is more than the length cutoff, truncate it and set that as the preview
        int CONTENT_LENGTH_CUTOFF = 50;
        if (fullContent.length() > CONTENT_LENGTH_CUTOFF)
        {
            return fullContent.substring(0, CONTENT_LENGTH_CUTOFF);
        }
        return fullContent;
    }

    private String determineChatName(AbstractChat abstractChat)
    {
        if (abstractChat instanceof GroupChat groupChat)
        {
            return groupChat.getChatName();
        }

        return null;
    }

    public void sendNewMessageNotification(AbstractChat chat, User sender, Message sentMessage) {
        String fullContent = sentMessage.getContent();

        String contentPreview = determineContentPreview(fullContent);

        String chatName = determineChatName(chat);

        NewMessageNotificationDTO newMessageNotificationDTO =
                new NewMessageNotificationDTO(chat.getId(), sender.getId(), sentMessage.getId(),
                        contentPreview, sentMessage.getCreatedAt().toString(), sender.getForumUsername(), chatName);

        //Send this DTO to every user that is subscribed to this channel.
        simpMessagingTemplate.convertAndSend(
                "/new_messages/" + chat.getId(),
                newMessageNotificationDTO
        );

    }

    public void sendNewChatNotification(AbstractChat newChat, User sender, Message sentMessage, User recipient){
        String fullContent = sentMessage.getContent();

        String contentPreview = determineContentPreview(fullContent);

        NewChatNotificationDTO newChatNotificationDTO = new NewChatNotificationDTO(newChat.getId(), sender.getId(), sentMessage.getId(),
                contentPreview, sender.getForumUsername());

        simpMessagingTemplate.convertAndSendToUser(recipient.getForumUsername(),
                "/new_chat_notifications",
                newChatNotificationDTO);
    }

    public void sendChatInvitationNotification(ChatInvitation sentInvitation)
    {
        GroupChat associatedChat = sentInvitation.getAssociatedChat();
        User sender =  sentInvitation.getSender();
        ChatInvitationNotificationDTO invitationNotificationDTO = new ChatInvitationNotificationDTO(
                sender.getForumUsername(), associatedChat.getChatName(),
                sentInvitation.getId(), sentInvitation.getCreatedAt().toString()
        );

        simpMessagingTemplate.convertAndSendToUser(
                sentInvitation.getRecipient().getForumUsername(),
                "/new_chat_invitations",
                invitationNotificationDTO
        );
    }

    public void sendFriendRequestNotification(FriendRequest sentFriendRequest)
    {
        User sender = sentFriendRequest.getSender();

        FriendRequestNotificationDTO notificationDTO = new FriendRequestNotificationDTO(
                sender.getForumUsername(),
                sentFriendRequest.getId(),
                sentFriendRequest.getCreatedAt().toString()
        );

        simpMessagingTemplate.convertAndSendToUser(
                sentFriendRequest.getRecipient().getForumUsername(),
                "/new_chat_invitations",
                notificationDTO
        );
    }

    public void sendKickNotification(GroupChat chat, ChatParticipation recipientParticipation)
    {
        User recipient = recipientParticipation.getParticipatingUser();

        KickedNotificationDTO kickedNotificationDTO = new KickedNotificationDTO(chat.getChatName(),
                chat.getId());

        simpMessagingTemplate.convertAndSendToUser(
                recipient.getForumUsername(),
                "/kick_notifications",
                kickedNotificationDTO
        );
    }

    public void sendFriendActionNotification(FriendRequestActionDTO actionDTO, String recipientUsername)
    {
        simpMessagingTemplate.convertAndSendToUser(
                recipientUsername,
                "/friendship_notifications",
                actionDTO
        );
    }
}

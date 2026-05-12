package com.rebuild.backend.service.forum_services;

import com.rebuild.backend.model.dtos.ChatInvitationNotificationDTO;
import com.rebuild.backend.model.dtos.FriendRequestNotificationDTO;
import com.rebuild.backend.model.dtos.NewMessageNotificationDTO;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.*;
import com.rebuild.backend.model.entities.user_entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

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

        //Only take the first 50 characters of a message if it is longer than that
        String contentPreview = determineContentPreview(fullContent);

        String chatName =  determineChatName(chat);

        NewMessageNotificationDTO newMessageNotificationDTO =
                new NewMessageNotificationDTO(chat.getId(), sender.getId(), sentMessage.getId(),
                        contentPreview, sentMessage.getCreatedAt(), sender.getForumUsername(), chatName);

        //Send this DTO to every user that is subscribed to this channel.
        simpMessagingTemplate.convertAndSend(
                "/new_messages/" + chat.getId(),
                newMessageNotificationDTO
        );

    }


    public void sendChatInvitationNotification(ChatInvitation sentInvitation)
    {
        GroupChat associatedChat = sentInvitation.getAssociatedChat();
        User sender =  sentInvitation.getSender();
        ChatInvitationNotificationDTO invitationNotificationDTO = new ChatInvitationNotificationDTO(
                sender.getForumUsername(), associatedChat.getChatName(),
                sentInvitation.getId(), sentInvitation.getCreatedAt()
        );

        simpMessagingTemplate.convertAndSendToUser(
                sentInvitation.getRecipient().getUsername(),
                "/user/new_chat_invitations",
                invitationNotificationDTO
        );
    }

    public void sendFriendRequestNotification(FriendRequest sentFriendRequest)
    {
        User sender = sentFriendRequest.getSender();

        FriendRequestNotificationDTO notificationDTO = new FriendRequestNotificationDTO(
                sender.getForumUsername(),
                sentFriendRequest.getId(),
                sentFriendRequest.getCreationTimestamp()
        );

        simpMessagingTemplate.convertAndSendToUser(
                sentFriendRequest.getRecipient().getUsername(),
                "/user/new_chat_invitations",
                notificationDTO
        );
    }
}

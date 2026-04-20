package com.rebuild.backend.service.forum_services;

import com.rebuild.backend.model.entities.messaging_and_friendship_entities.AbstractChat;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.ChatParticipation;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.GroupChat;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.PrivateChat;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.service.util_services.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ChatUtilService {

    private final CloudinaryService cloudinaryService;

    @Autowired
    public ChatUtilService(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    public User determineOtherChatUser(PrivateChat chat, User loadingUser)
    {
        return loadingUser.equals(chat.getRecipient()) ? chat.getRecipient() : chat.getSender();
    }

    public String determineChatDisplayName(AbstractChat chat, User loadingUser)
    {
        if (chat instanceof GroupChat groupChat)
        {
            return groupChat.getChatName();
        }
        if (chat instanceof PrivateChat privateChat)
        {
            //Otherwise, the display name will be the forum username of the "other" user in the chat
            User otherUser = determineOtherChatUser(privateChat, loadingUser);
            return "Chat with " + otherUser.getForumUsername();
        }

        //Should never get here
        return null;
    }

    public String determineChatPictureUrl(AbstractChat chat, User loadingUser)
    {
        if (chat instanceof GroupChat groupChat)
        {
            String chatPictureId = groupChat.getPictureId();
            if (chatPictureId == null)
            {
                return null;
            }
            return cloudinaryService.generateTimedUrlForPictureId(chatPictureId);
        }

        if (chat instanceof PrivateChat privateChat)
        {
            User otherUser = determineOtherChatUser(privateChat, loadingUser);
            String pictureUrl = otherUser.getUserProfile().getPictureId();
            if (pictureUrl == null)
            {
                return null;
            }
            return cloudinaryService.generateTimedUrlForPictureId(pictureUrl);
        }

        //Should never get here.
        return null;
    }

    public int determineUnreadMessageCount(AbstractChat abstractChat, User user)
    {
        if (abstractChat instanceof GroupChat groupChat)
        {
            return groupChat.getParticipations().stream().
                    dropWhile(participation ->
                            !participation.getParticipatingUser().equals(user)).findFirst()
                    .map(ChatParticipation::getUnreadMessagesCount).orElse(0);
        }

        if (abstractChat instanceof PrivateChat privateChat)
        {
            if (privateChat.getSender().equals(user))
            {
                return privateChat.getSenderUnreadMessages();
            }

            if (privateChat.getRecipient().equals(user))
            {
                return privateChat.getRecipientUnreadMessages();
            }

        }

        return 0;
    }
}

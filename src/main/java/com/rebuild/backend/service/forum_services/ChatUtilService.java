package com.rebuild.backend.service.forum_services;

import com.rebuild.backend.model.entities.messaging_and_friendship_entities.GroupChatPicture;
import com.rebuild.backend.model.entities.user_entities.UserProfilePicture;
import com.rebuild.backend.model.entities.util_entitites.base_entities.AbstractChat;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.ChatParticipation;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.GroupChat;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.PrivateChat;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.repository.messaging_and_friendship_repositories.ChatParticipationRepository;
import com.rebuild.backend.service.util_services.AWSService;
import com.rebuild.backend.utils.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ChatUtilService {

    private final AWSService awsService;

    private final ChatParticipationRepository participationRepository;

    @Autowired
    public ChatUtilService(AWSService awsService, ChatParticipationRepository participationRepository) {
        this.awsService = awsService;
        this.participationRepository = participationRepository;
    }

    public User determineOtherChatUser(PrivateChat chat, User loadingUser)
    {
        return chat.getParticipations().stream().filter(chatParticipation ->
                !chatParticipation.getParticipatingUser().equals(loadingUser)).findFirst()
                .map(ChatParticipation::getParticipatingUser).orElse(null);
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
            GroupChatPicture picture = groupChat.getPicture();
            if (picture == null)
            {
                return null;
            }
            return awsService.generateDownloadUrlForPicture(picture);
        }

        if (chat instanceof PrivateChat privateChat)
        {
            User otherUser = determineOtherChatUser(privateChat, loadingUser);
            UserProfilePicture picture = otherUser.getUserProfile().getPicture();
            if (picture == null)
            {
                return null;
            }
            return awsService.generateDownloadUrlForPicture(picture);
        }

        //Should never get here.
        return null;
    }

    public int determineUnreadMessageCount(AbstractChat abstractChat, User user)
    {
        return abstractChat.getParticipations().stream().
                dropWhile(participation ->
                        !participation.getParticipatingUser().equals(user)).findFirst()
                .map(ChatParticipation::getUnreadMessagesCount).orElse(0);

    }
}

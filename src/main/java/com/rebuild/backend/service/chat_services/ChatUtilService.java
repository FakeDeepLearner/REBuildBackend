package com.rebuild.backend.service.chat_services;

import com.rebuild.backend.model.entities.util_entitites.AbstractChat;
import com.rebuild.backend.model.entities.chat_entities.ChatParticipation;
import com.rebuild.backend.model.entities.chat_entities.GroupChat;
import com.rebuild.backend.model.entities.chat_entities.PrivateChat;
import com.rebuild.backend.model.entities.user_entities.User;

import com.rebuild.backend.repository.messaging_and_friendship_repositories.ChatParticipationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class ChatUtilService {

    private final ChatParticipationRepository participationRepository;

    @Autowired
    public ChatUtilService(ChatParticipationRepository participationRepository) {
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

        if (chat instanceof PrivateChat privateChat)
        {
            User otherUser = determineOtherChatUser(privateChat, loadingUser);
            return otherUser.getImageUrl();
        }

        //A group chat will not have a picture anymore.
        return null;
    }

    public int determineUnreadMessageCount(AbstractChat abstractChat, User user)
    {
        return abstractChat.getParticipations().stream().
                dropWhile(participation ->
                        !participation.getParticipatingUser().equals(user)).findFirst()
                .map(ChatParticipation::getUnreadMessagesCount).orElse(0);

    }

    // A helper method for a user to join a group chat. When this method runs,
    // we assume that all the necessary checks have been done
    public ChatParticipation addUserToChat(GroupChat chatToJoin, User joiningUser)
    {
        ChatParticipation recipientParticipation = new ChatParticipation(joiningUser,
                chatToJoin, false);
        recipientParticipation.setLastMessage(chatToJoin.getLastMessage());

        chatToJoin.setMemberCount(chatToJoin.getMemberCount() + 1);
        chatToJoin.getParticipations().add(recipientParticipation);
        joiningUser.addChatParticipation(recipientParticipation);

        return recipientParticipation;

    }

    //Same as above, but this time the user is removed from the chat.
    public void removeUserFromChat(GroupChat chat, ChatParticipation kickedUserParticipation)
    {
        chat.setMemberCount(chat.getMemberCount() - 1);
        chat.getParticipations().remove(kickedUserParticipation);
        User kickedUser = kickedUserParticipation.getParticipatingUser();
        kickedUser.getChatParticipations().remove(kickedUserParticipation);

        participationRepository.delete(kickedUserParticipation);
    }
}

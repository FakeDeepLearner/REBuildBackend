package com.rebuild.backend.service.chat_services;

import com.rebuild.backend.model.dtos.forum_dtos.message_and_chat_dtos.MessageDisplayDTO;
import com.rebuild.backend.model.entities.chat_entities.Message;
import com.rebuild.backend.model.entities.util_entitites.AbstractChat;
import com.rebuild.backend.model.entities.chat_entities.ChatParticipation;
import com.rebuild.backend.model.entities.chat_entities.GroupChat;
import com.rebuild.backend.model.entities.chat_entities.PrivateChat;
import com.rebuild.backend.model.entities.user_entities.User;

import com.rebuild.backend.model.responses.forum_responses.DisplayChatResponse;
import com.rebuild.backend.model.responses.forum_responses.LoadChatResponse;
import com.rebuild.backend.repository.messaging_and_friendship_repositories.ChatParticipationRepository;
import com.rebuild.backend.repository.messaging_and_friendship_repositories.MessageRepository;
import com.rebuild.backend.service.util_services.WebsocketsService;
import com.rebuild.backend.utils.exceptions.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Transactional
public class ChatUtilService {

    private final ChatParticipationRepository participationRepository;

    private final MessageRepository messageRepository;

    private final WebsocketsService websocketsService;

    @Autowired
    public ChatUtilService(ChatParticipationRepository participationRepository, MessageRepository messageRepository, WebsocketsService websocketsService) {
        this.participationRepository = participationRepository;
        this.messageRepository = messageRepository;
        this.websocketsService = websocketsService;
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

    public LoadChatResponse loadChat(AbstractChat chat, User loadingUser, int pageNumber)
    {
        ChatParticipation userParticipation = participationRepository.
                findByParticipatingUserAndAssociatedChat(loadingUser, chat).
                orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "You are not participating in this chat, you can't load it"));

        //Update the participation of this user in this chat.
        userParticipation.setUnreadMessagesCount(0);
        userParticipation.setLastMessage(chat.getLastMessage());
        participationRepository.save(userParticipation);

        String chatDisplayName = determineChatDisplayName(chat, loadingUser);
        String chatPictureUrl = determineChatPictureUrl(chat, loadingUser);

        Pageable request = PageRequest.of(pageNumber, 30, Sort.by(Sort.Direction.DESC, "createdAt"));

        Slice<Message> currentMessages = messageRepository.findByAssociatedChat(chat, request);

        List<MessageDisplayDTO> messages = currentMessages.getContent()
                .stream().
                map(message -> message.toDTo(loadingUser)).toList();

        return new LoadChatResponse(chatDisplayName, chat.getId(), messages,
                chatPictureUrl, currentMessages.hasNext(), userParticipation.getIsAdmin());
    }

    public Message createNewMessageInChat(AbstractChat chat, User creatingUser, String messageContent,
                                          boolean sendWebsocketNotification)
    {
        Message newMessage = new Message(creatingUser, messageContent);
        newMessage.setAssociatedChat(chat);
        chat.getMessages().add(newMessage);
        chat.setLastMessage(messageContent);

        List<ChatParticipation> newParticipations = chat.getParticipations().stream().
                peek(participation -> {
                    //Any muted participations are not updated at all.
                    if (participation.isMuted())
                    {
                        return;
                    }
                    if (creatingUser.equals(participation.getParticipatingUser()))
                    {
                        participation.setLastMessage(messageContent);
                    }
                    //For participations that do not belong to the sender, we increase their unread count and
                    // update their last message only if the chat has not been muted
                    else
                    {
                        participation.setLastMessage(messageContent);
                        participation.setUnreadMessagesCount(participation.getUnreadMessagesCount() + 1);
                    }

                }).collect(Collectors.toCollection(ArrayList::new));
        chat.setParticipations(newParticipations);

        Message savedMessage = messageRepository.save(newMessage);
        if (sendWebsocketNotification)
        {
            websocketsService.sendNewMessageNotification(chat, creatingUser, savedMessage);
        }

        return savedMessage;
    }

    public List<DisplayChatResponse> displayChatResponses(List<ChatParticipation> participations, User displayingUser)
    {
        return participations.stream()
                .map(participation -> {
                    AbstractChat participatedChat = participation.getAssociatedChat();

                    int unreadMessageCount = determineUnreadMessageCount(participatedChat, displayingUser);

                    String chatDisplayName = determineChatDisplayName(participatedChat, displayingUser);

                    String chatPictureUrl = determineChatPictureUrl(participatedChat, displayingUser);

                    return new DisplayChatResponse(participatedChat.getId(), chatDisplayName,
                            chatPictureUrl, Objects.requireNonNullElse(participation.getLastMessage(),
                            participatedChat.getLastMessage()),
                            unreadMessageCount, participation.isMuted());
                }).collect(Collectors.toCollection(ArrayList::new));
    }
}

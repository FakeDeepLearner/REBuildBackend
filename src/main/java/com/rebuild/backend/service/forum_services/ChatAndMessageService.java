package com.rebuild.backend.service.forum_services;

import com.rebuild.backend.model.dtos.StatusAndError;
import com.rebuild.backend.model.dtos.forum_dtos.MessageDisplayDTO;
import com.rebuild.backend.model.dtos.forum_dtos.NewMessageDTO;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.Chat;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.ChatParticipation;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.FriendRelationship;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.Message;
import com.rebuild.backend.model.entities.profile_entities.ProfilePicture;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.responses.DisplayChatResponse;
import com.rebuild.backend.model.responses.LoadChatResponse;
import com.rebuild.backend.repository.forum_repositories.*;
import com.rebuild.backend.repository.user_repositories.ProfilePictureRepository;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import com.rebuild.backend.service.util_services.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ChatAndMessageService {

    private final ChatRepository chatRepository;

    private final UserRepository userRepository;

    private final FriendRelationshipRepository friendRelationshipRepository;

    private final CloudinaryService cloudinaryService;

    private final ChatParticipationRepository participationRepository;

    @Autowired
    public ChatAndMessageService(ChatRepository chatRepository, UserRepository userRepository,
                                 FriendRelationshipRepository friendRelationshipRepository,
                                 CloudinaryService cloudinaryService,
                                 ChatParticipationRepository participationRepository) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.friendRelationshipRepository = friendRelationshipRepository;
        this.cloudinaryService = cloudinaryService;
        this.participationRepository = participationRepository;
    }


    private Chat createChatBetween(User sender, User recipient)
    {
        Chat newChat = new Chat();
        ChatParticipation senderParticipation = new ChatParticipation(sender, newChat, true, Instant.now());
        ChatParticipation recipientParticipation = new ChatParticipation(recipient, newChat, false, Instant.now());
        newChat.setParticipations(List.of(senderParticipation, recipientParticipation));
        sender.addChatParticipation(senderParticipation);
        recipient.addChatParticipation(recipientParticipation);

        return chatRepository.save(newChat);
    }

    private NewMessageDTO sendMessageTo(User sender, User recipient, String messageContent){
        //If we enter this method, we know that we will have to create a new chat.
        Chat createdChat = createChatBetween(sender, recipient);

        Message newMessage = new Message(sender, messageContent);
        newMessage.setAssociatedChat(createdChat);
        createdChat.getMessages().add(newMessage);

        return new NewMessageDTO(chatRepository.save(createdChat), newMessage);
    }

    private NewMessageDTO sendMessageTo(User sender, String content,
                                        Chat associatedChat)
    {

        Message newMessage = new Message(sender, content);
        newMessage.setAssociatedChat(associatedChat);
        associatedChat.getMessages().add(newMessage);
        associatedChat.setLastMessage(content);

        List<ChatParticipation> otherChatParticipations = associatedChat.getParticipations().stream().
                filter(participation -> !sender.equals(participation.getParticipatingUser())).
                toList();

        otherChatParticipations.forEach(participation ->
                participation.setUnreadMessagesCount(participation.getUnreadMessagesCount() + 1));

        return new NewMessageDTO(chatRepository.save(associatedChat), newMessage);
    }


    public NewMessageDTO createMessage(User sender, UUID recipientId, String messageContent)
    {
        User recipient = userRepository.findById(recipientId).orElse(null);
        assert recipient != null : "User with this ID not found";

        // If the 2 users have a chat together,
        // immediately send the message no matter the recipient's profile settings
        Optional<Chat> foundChat = chatRepository.findByTwoUsers(sender, recipient);
        if (foundChat.isPresent()) {
            return sendMessageTo(sender, messageContent, foundChat.get());
        }

        // If the recipient has not selected the setting, just send the message with the content,
        // creating a chat between the users first
        if(!recipient.getUserProfile().getSettings().isMessagesFromFriendsOnly())
        {
            return sendMessageTo(sender, recipient, messageContent);
        }


        Optional<FriendRelationship> foundRelationship =
                friendRelationshipRepository.findByTwoUsers(sender, recipient);
        if (foundRelationship.isPresent()) {
            return sendMessageTo(sender, recipient, messageContent);
        }
        return null;

    }


    private User determineOtherChatUser(Chat chat, User loadingUser)
    {
        return chat.getParticipations().stream().map(ChatParticipation::getParticipatingUser)
                .filter(user -> !user.equals(loadingUser)).findFirst().orElseThrow();
    }

    private String determineChatDisplayName(Chat chat, User loadingUser)
    {
        //If this is a group chat, the display name will be the name of the chat.
        if (chat.getChatName() != null)
        {
            return chat.getChatName();
        }
        //Otherwise, the display name will be the forum username of the "other" user in the chat
        User otherUser = determineOtherChatUser(chat, loadingUser);
        return otherUser.getForumUsername();
    }

    private String determineChatPictureUrl(Chat chat, User loadingUser)
    {
        //If this chat is a group chat, just return its profile picture url if it has one.
        if (chat.getChatName() != null)
        {
            ProfilePicture chatPicture = chat.getChatPicture();
            if (chatPicture == null)
            {
                return null;
            }
            return cloudinaryService.generateTimedUrlForPicture(chatPicture);
        }
        //Otherwise, just return the "other" person's profile picture's url.
        else
        {
            User otherUser = determineOtherChatUser(chat, loadingUser);
            ProfilePicture picture = otherUser.getUserProfile().getProfilePicture();
            if (picture == null)
            {
                return null;
            }
            return cloudinaryService.generateTimedUrlForPicture(picture);

        }
    }

    public List<DisplayChatResponse> displayAllChats(User displayingUser)
    {
        List<ChatParticipation> userParticipations = participationRepository.
                findParticipationsByUser(displayingUser);

        return userParticipations.stream()
                .map(participation -> {
                    Chat participatedChat = participation.getParticipatedChat();

                    String chatDisplayName = determineChatDisplayName(participatedChat, displayingUser);

                    String chatPictureUrl = determineChatPictureUrl(participatedChat, displayingUser);

                    return new DisplayChatResponse(participatedChat.getId(), chatDisplayName,
                            chatPictureUrl, participatedChat.getLastMessage(),
                            participation.getUnreadMessagesCount());
                }).toList();
    }



    public LoadChatResponse loadChat(UUID chatId, User loadingUser)
    {
        Chat chat = chatRepository.findByIdWithMessages(chatId).orElse(null);
        assert chat != null : "Chat with this ID is not found";
        String chatDisplay = determineChatDisplayName(chat, loadingUser);
        String chatPictureUrl = determineChatPictureUrl(chat, loadingUser);

        List<MessageDisplayDTO> messages = chat.getMessages()
                .stream().
                map(message -> {
                    boolean displayOnTheRight = message.getSender().equals(loadingUser);
                    return new MessageDisplayDTO(message.getContent(), message.getCreatedAt(),
                            displayOnTheRight);
                }).toList();

        return new LoadChatResponse(chatDisplay, chat.getId(), messages, chatPictureUrl);
    }
}

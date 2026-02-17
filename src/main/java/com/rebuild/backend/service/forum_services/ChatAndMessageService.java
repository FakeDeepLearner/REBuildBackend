package com.rebuild.backend.service.forum_services;

import com.rebuild.backend.model.dtos.forum_dtos.MessageDisplayDTO;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.Chat;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.FriendRelationship;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.Message;
import com.rebuild.backend.model.entities.profile_entities.ProfilePicture;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.responses.DisplayChatResponse;
import com.rebuild.backend.model.responses.LoadChatResponse;
import com.rebuild.backend.repository.forum_repositories.ChatRepository;
import com.rebuild.backend.repository.forum_repositories.FriendRelationshipRepository;
import com.rebuild.backend.repository.forum_repositories.MessageRepository;
import com.rebuild.backend.repository.user_repositories.ProfilePictureRepository;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import com.rebuild.backend.service.util_services.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ChatAndMessageService {

    private final ChatRepository chatRepository;

    private final MessageRepository messageRepository;

    private final UserRepository userRepository;

    private final FriendRelationshipRepository friendRelationshipRepository;

    private final ProfilePictureRepository profilePictureRepository;

    private final CloudinaryService cloudinaryService;

    @Autowired
    public ChatAndMessageService(ChatRepository chatRepository, MessageRepository messageRepository,
                                 UserRepository userRepository, FriendRelationshipRepository friendRelationshipRepository,
                                 ProfilePictureRepository profilePictureRepository, CloudinaryService cloudinaryService) {
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.friendRelationshipRepository = friendRelationshipRepository;
        this.profilePictureRepository = profilePictureRepository;
        this.cloudinaryService = cloudinaryService;
    }

    private Chat createChatBetween(User sender, User recipient)
    {
        Chat newChat = new Chat(sender, recipient);
        sender.addSenderChat(newChat);
        recipient.addReceiverChat(newChat);
        return chatRepository.save(newChat);
    }

    private Message createNewMessage(User sender, User recipient, String messageContent){
        //If we enter this method, we know that we will have to create a new chat.
        Chat createdChat = createChatBetween(sender, recipient);

        Message newMessage = new Message(sender, recipient, messageContent);
        newMessage.setAssociatedChat(createdChat);
        createdChat.getMessages().add(newMessage);

        return messageRepository.save(newMessage);
    }

    private Message createNewMessage(User sender, User recipient, String content,
                                     Chat associatedChat)
    {

        Message newMessage = new Message(sender, recipient, content);
        newMessage.setAssociatedChat(associatedChat);
        associatedChat.getMessages().add(newMessage);

        return messageRepository.save(newMessage);
    }


    public Message createMessage(User sender, UUID recipientId, String messageContent)
    {
        User recipient = userRepository.findById(recipientId).orElse(null);
        assert recipient != null : "User with this ID not found";

        // If the 2 users have a chat together,
        // immediately send the message no matter the recipient's profile settings
        Optional<Chat> foundChat = chatRepository.findByTwoUsers(sender, recipient);
        if (foundChat.isPresent()) {
            return createNewMessage(sender, recipient, messageContent, foundChat.get());
        }

        // If the recipient has not selected the setting, just send the message with the content,
        // creating a chat between the users first
        if(!recipient.getUserProfile().getSettings().getMessagesFromFriendsOnly())
        {
            return createNewMessage(sender, recipient, messageContent);
        }


        Optional<FriendRelationship> foundRelationship =
                friendRelationshipRepository.findByTwoUsers(sender, recipient);
        if (foundRelationship.isPresent()) {
            return createNewMessage(sender, recipient, messageContent);
        }
        return null;

    }


    public List<DisplayChatResponse> displayAllChats(User displayingUser)
    {
        List<Chat> allChats = chatRepository.findByUser(displayingUser);

        return allChats.stream()
                .map(chat -> {
                    User chatInitiator = chat.getInitiatingUser();
                    User chatReceiver = chat.getReceivingUser();


                    User otherChatUser = chatInitiator.equals(displayingUser) ? chatReceiver : chatInitiator;

                    String picture_url = profilePictureRepository.findByUserId(otherChatUser.getId()).
                            map(cloudinaryService::generateTimedUrlForPicture).orElse(null);
                    UUID chatId = chat.getId();
                    String username = otherChatUser.getForumUsername();

                    return new DisplayChatResponse(chatId, username, picture_url);
                }).toList();
    }


    public LoadChatResponse loadChat(UUID chatId, User loadingUser)
    {
        Chat chat = chatRepository.findByIdWithMessages(chatId).orElse(null);
        assert chat != null : "Chat with this ID not found";
        User receiver = chat.getReceivingUser();
        UUID userID = receiver.getId();
        String userName = receiver.getForumUsername();
        Optional<ProfilePicture> foundPicture = profilePictureRepository.findByUserId(userID);

        List<MessageDisplayDTO> messages = chat.getMessages()
                .stream().
                map(message -> {
                    boolean displayOnTheRight = message.getSender().equals(loadingUser);
                    return new MessageDisplayDTO(message.getContent(), message.getCreatedAt(),
                            displayOnTheRight);
                }).toList();


        String pictureUrl = foundPicture.map(cloudinaryService::generateTimedUrlForPicture).orElse(null);
        return new LoadChatResponse(userName, userID, messages, pictureUrl);
    }
}

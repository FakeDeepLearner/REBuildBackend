package com.rebuild.backend.service.forum_services;

import com.rebuild.backend.model.entities.messaging_and_friendship_entities.Chat;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.FriendRelationship;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.FriendRequest;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.Message;
import com.rebuild.backend.model.entities.profile_entities.ProfilePicture;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.forms.dtos.forum_dtos.MessageDisplayDTO;
import com.rebuild.backend.model.responses.DisplayChatResponse;
import com.rebuild.backend.model.responses.LoadChatResponse;
import com.rebuild.backend.repository.forum_repositories.ChatRepository;
import com.rebuild.backend.repository.forum_repositories.FriendRelationshipRepository;
import com.rebuild.backend.repository.forum_repositories.FriendRequestRepository;
import com.rebuild.backend.repository.forum_repositories.MessageRepository;
import com.rebuild.backend.repository.user_repositories.ProfilePictureRepository;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FriendAndMessageService {

    private final UserRepository userRepository;
    private final FriendRelationshipRepository friendRelationshipRepository;

    private final ChatRepository chatRepository;

    private final MessageRepository messageRepository;

    private final ProfilePictureRepository profilePictureRepository;

    private final FriendRequestRepository friendRequestRepository;

    @Autowired
    public FriendAndMessageService(UserRepository userRepository,
                                   FriendRelationshipRepository friendRelationshipRepository,
                                   ChatRepository chatRepository, MessageRepository messageRepository, ProfilePictureRepository profilePictureRepository, FriendRequestRepository friendRequestRepository) {
        this.userRepository = userRepository;
        this.friendRelationshipRepository = friendRelationshipRepository;
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
        this.profilePictureRepository = profilePictureRepository;
        this.friendRequestRepository = friendRequestRepository;
    }

    //Friendship is symmetric, so it doesn't matter for this method who the users are
    public void addFriend(User sender, User recipient)
    {
        FriendRelationship friendRelationship = new FriendRelationship(sender, recipient);

        friendRelationshipRepository.save(friendRelationship);
    }


    public FriendRequest sendFriendRequest(User sender, UUID recipientId)
    {
        User recipient = userRepository.findById(recipientId).orElseThrow();

        friendRelationshipRepository.findByTwoUsers(sender, recipient).
                ifPresent(l ->
                {
                    throw new AssertionError("Friendship already exists");
                });

        FriendRequest newRequest = new FriendRequest(sender, recipient);

        return friendRequestRepository.save(newRequest);
    }

    private Chat createChatBetween(User sender, User recipient)
    {
        Chat newChat = new Chat(sender, recipient);
        sender.addSenderChat(newChat);
        recipient.addReceiverChat(newChat);
        return chatRepository.save(newChat);
    }


    public Message createMessage(User sender, UUID recipientId, String messageContent)
    {
        User recipient = userRepository.findById(recipientId).orElse(null);
        assert recipient != null : "User with this ID not found";

        //If we have a chat between these 2 users, use it. If we don't, create one.
        Chat createdOrFoundChat = chatRepository.findByTwoUsers(sender, recipient).
                orElse(createChatBetween(sender, recipient));

        Message newMessage = new Message(sender, recipient, messageContent);
        newMessage.setAssociatedChat(createdOrFoundChat);
        createdOrFoundChat.getMessages().add(newMessage);

        return messageRepository.save(newMessage);
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
                            map(ProfilePicture::getSecure_url).orElse(null);
                    UUID chatId = chat.getId();
                    String username = otherChatUser.getUsername();

                    return new DisplayChatResponse(chatId, username, picture_url);
                }).toList();
    }

    public LoadChatResponse loadChat(UUID chatId, User loadingUser)
    {
        Chat chat = chatRepository.findById(chatId).orElse(null);
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


        String pictureUrl = foundPicture.map(ProfilePicture::getSecure_url).orElse(null);
        return new LoadChatResponse(userName, userID, messages, pictureUrl);
    }


}

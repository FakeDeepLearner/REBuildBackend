package com.rebuild.backend.service.forum_services;

import com.rebuild.backend.model.dtos.StatusAndError;
import com.rebuild.backend.model.dtos.forum_dtos.FriendRequestDTO;
import com.rebuild.backend.model.dtos.forum_dtos.MessageDisplayDTO;
import com.rebuild.backend.model.dtos.forum_dtos.NewMessageDTO;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.*;
import com.rebuild.backend.model.entities.profile_entities.ProfilePicture;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.exceptions.BelongingException;
import com.rebuild.backend.model.responses.DisplayChatResponse;
import com.rebuild.backend.model.responses.LoadChatResponse;
import com.rebuild.backend.repository.forum_repositories.*;
import com.rebuild.backend.repository.user_repositories.ProfilePictureRepository;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import com.rebuild.backend.service.util_services.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class ChatAndMessageService {

    private final ChatRepository chatRepository;

    private final UserRepository userRepository;

    private final FriendRelationshipRepository friendRelationshipRepository;

    private final CloudinaryService cloudinaryService;

    private final ChatParticipationRepository participationRepository;

    private final ChatInvitationRepository chatInvitationRepository;

    @Autowired
    public ChatAndMessageService(ChatRepository chatRepository, UserRepository userRepository,
                                 FriendRelationshipRepository friendRelationshipRepository,
                                 CloudinaryService cloudinaryService,
                                 ChatParticipationRepository participationRepository,
                                 ChatInvitationRepository chatInvitationRepository) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.friendRelationshipRepository = friendRelationshipRepository;
        this.cloudinaryService = cloudinaryService;
        this.participationRepository = participationRepository;
        this.chatInvitationRepository = chatInvitationRepository;
    }


    public Chat createNewGroupChat(User creatingUser, String chatName)
    {
        Chat newChat = new Chat();

        ChatParticipation userParticipation = new ChatParticipation(creatingUser, newChat, true);

        newChat.setChatName(chatName);
        newChat.setParticipations(new ArrayList<>(List.of(userParticipation)));

        return chatRepository.save(newChat);
    }

    @Transactional
    public StatusAndError acceptChatInvitation(User recipient, UUID invitationId)
    {
        ChatInvitation foundInvitation = chatInvitationRepository.findByIdAndRecipient(invitationId,
                recipient).orElseThrow(() ->
                new BelongingException("This invitation either does not exist or does not belong to you"));

        Chat associatedChat = foundInvitation.getAssociatedChat();

        ChatParticipation recipientParticipation = new ChatParticipation(recipient,
                associatedChat, false);

        associatedChat.getParticipations().add(recipientParticipation);

        chatInvitationRepository.delete(foundInvitation);

        chatRepository.save(associatedChat);

        return new StatusAndError(HttpStatus.CREATED, "You are now a member of " + associatedChat.getChatName());
    }

    public void declineChatInvitation(User recipient, UUID invitationId)
    {
        ChatInvitation foundInvitation = chatInvitationRepository.findByIdAndRecipient(invitationId,
                recipient).orElseThrow(() ->
                new BelongingException("This invitation either does not exist or does not belong to you"));

        chatInvitationRepository.delete(foundInvitation);
    }

    @Transactional
    public StatusAndError sendGroupChatInvitation(User sender, UUID recipientId, UUID chatId)
    {
        User recipient = userRepository.findById(recipientId).orElse(null);

        Chat foundChat = chatRepository.findById(chatId).orElse(null);

        assert recipient != null : "Recipient not found";

        assert foundChat != null : "Chat not found";

        if (foundChat.getChatName() == null)
        {
            return new StatusAndError(HttpStatus.FORBIDDEN, "This chat is not a group chat, " +
                    "you can't invite users to it");
        }


        Optional<ChatInvitation> foundInvitation =
                chatInvitationRepository.findBySenderAndRecipientAndAssociatedChat_Id(sender, recipient,
                        chatId);

        if (foundInvitation.isPresent()) {
            return new StatusAndError(HttpStatus.CONFLICT,
                    "You already have an existing group chat invitation with this user, you cannot send " +
                            "another one.");
        }



        ChatInvitation newInvitation = new ChatInvitation(sender, recipient, foundChat);

        chatInvitationRepository.save(newInvitation);

        return new StatusAndError(HttpStatus.OK, "The invitation has been sent");
    }


    private Chat createChatBetween(User sender, User recipient)
    {
        Chat newChat = new Chat();
        ChatParticipation senderParticipation = new ChatParticipation(sender, newChat, true);
        ChatParticipation recipientParticipation = new ChatParticipation(recipient, newChat, false);
        newChat.setParticipations(new ArrayList<>(List.of(senderParticipation, recipientParticipation)));
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


    public NewMessageDTO createMessage(User sender, UUID receivingObjectId, String messageContent)
    {
        Optional<User> recipient = userRepository.findById(receivingObjectId);

        if (recipient.isPresent())
        {
           User receivingUser = recipient.get();

            // If the recipient has not selected the setting, just send the message with the content,
            // creating a chat between the users first
            if(!receivingUser.getUserProfile().getSettings().isMessagesFromFriendsOnly())
            {
                return sendMessageTo(sender, receivingUser, messageContent);
            }


            Optional<FriendRelationship> foundRelationship =
                    friendRelationshipRepository.findByTwoUsers(sender, receivingUser);
            if (foundRelationship.isPresent()) {
                return sendMessageTo(sender, receivingUser, messageContent);
            }
        }


        Optional<Chat> recipientChat = chatRepository.findById(receivingObjectId);

        return recipientChat.map(chat ->
                sendMessageTo(sender, messageContent, chat)).orElse(null);

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

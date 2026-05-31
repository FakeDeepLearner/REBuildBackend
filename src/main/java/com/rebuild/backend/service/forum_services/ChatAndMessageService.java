package com.rebuild.backend.service.forum_services;

import com.rebuild.backend.model.dtos.forum_dtos.MessageDisplayDTO;
import com.rebuild.backend.model.dtos.forum_dtos.NewMessageDTO;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.*;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.entities.util_entitites.base_entities.AbstractChat;
import com.rebuild.backend.repository.messaging_and_friendship_repositories.*;
import com.rebuild.backend.utils.exceptions.BelongingException;
import com.rebuild.backend.utils.exceptions.ChatException;
import com.rebuild.backend.utils.exceptions.NotFoundException;
import com.rebuild.backend.model.responses.DisplayChatResponse;
import com.rebuild.backend.model.responses.LoadChatResponse;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ChatAndMessageService {

    private final WebsocketsService websocketsService;

    private final ChatRepository chatRepository;

    private final UserRepository userRepository;

    private final FriendRelationshipRepository friendRelationshipRepository;

    private final ChatParticipationRepository participationRepository;

    private final ChatInvitationRepository chatInvitationRepository;

    private final ChatUtilService chatUtilService;

    private final MessageRepository messageRepository;

    @Autowired
    public ChatAndMessageService(WebsocketsService websocketsService, ChatRepository chatRepository, UserRepository userRepository,
                                 FriendRelationshipRepository friendRelationshipRepository,
                                 ChatParticipationRepository participationRepository,
                                 ChatInvitationRepository chatInvitationRepository, ChatUtilService chatUtilService, MessageRepository messageRepository) {
        this.websocketsService = websocketsService;
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.friendRelationshipRepository = friendRelationshipRepository;
        this.participationRepository = participationRepository;
        this.chatInvitationRepository = chatInvitationRepository;
        this.chatUtilService = chatUtilService;
        this.messageRepository = messageRepository;
    }


    public GroupChat createNewGroupChat(User creatingUser, String chatName)
    {
        GroupChat newChat = new GroupChat();

        ChatParticipation userParticipation = new ChatParticipation(creatingUser, newChat);
        creatingUser.addChatParticipation(userParticipation);

        newChat.setChatName(chatName);
        newChat.setParticipations(new ArrayList<>(List.of(userParticipation)));

        return chatRepository.save(newChat);
    }

    @Transactional
    public GroupChat acceptChatInvitation(User recipient, UUID invitationId)
    {
        ChatInvitation foundInvitation = chatInvitationRepository.findByIdAndRecipient(invitationId,
                recipient).orElseThrow(() ->
                new BelongingException("This invitation either does not exist or does not belong to you"));

        GroupChat associatedChat = foundInvitation.getAssociatedChat();

        ChatParticipation recipientParticipation = new ChatParticipation(recipient,
                associatedChat);
        recipientParticipation.setLastMessage(associatedChat.getLastMessage());

        associatedChat.getParticipations().add(recipientParticipation);
        recipient.addChatParticipation(recipientParticipation);

        chatInvitationRepository.delete(foundInvitation);

        return chatRepository.save(associatedChat);

    }

    public void declineChatInvitation(User recipient, UUID invitationId)
    {
        ChatInvitation foundInvitation = chatInvitationRepository.findByIdAndRecipient(invitationId,
                recipient).orElseThrow(() ->
                new BelongingException("This invitation either does not exist or does not belong to you"));

        chatInvitationRepository.delete(foundInvitation);
    }

    @Transactional
    public ChatInvitation sendGroupChatInvitation(User sender, UUID recipientId, UUID chatId)
    {
        User recipient = userRepository.findById(recipientId).orElseThrow(
                () -> new NotFoundException("User with the specified id not found"));

        AbstractChat foundChat = chatRepository.findById(chatId).orElseThrow(() -> new NotFoundException(
                "Chat with the specified id not found"
        ));

        if (!(foundChat instanceof GroupChat))
        {
            throw new ChatException(HttpStatus.FORBIDDEN,
                    "This chat is not a group chat, so you can't invite anyone to it");
        }


        Optional<ChatInvitation> foundInvitation =
                chatInvitationRepository.findBySenderAndRecipientAndAssociatedChat_Id(sender, recipient,
                        chatId);

        if (foundInvitation.isPresent()) {
            throw new ChatException(HttpStatus.CONFLICT,
                    "You already have an existing group chat invitation with this user, you cannot send " +
                            "another one.");
        }


        //This cast is safe, since we already know that it isn't a private chat by this point. 
        ChatInvitation newInvitation = new ChatInvitation(sender, recipient, (GroupChat) foundChat);

        ChatInvitation savedInvitation = chatInvitationRepository.save(newInvitation);

        websocketsService.sendChatInvitationNotification(savedInvitation);
        return savedInvitation;
    }

    private NewMessageDTO sendMessageTo(User sender, User recipient, String messageContent){
        //If we enter this method, we know that we will have to create a new chat.
        PrivateChat createdChat = new PrivateChat(sender, recipient, messageContent);

        Message newMessage = new Message(sender, messageContent);
        newMessage.setAssociatedChat(createdChat);
        createdChat.getMessages().add(newMessage);
        createdChat.setLastMessage(messageContent);


        return new NewMessageDTO(chatRepository.save(createdChat), newMessage);
    }

    private NewMessageDTO sendMessageTo(User sender, String content,
                                        AbstractChat associatedChat)
    {

        Message newMessage = new Message(sender, content);
        newMessage.setAssociatedChat(associatedChat);
        associatedChat.getMessages().add(newMessage);
        associatedChat.setLastMessage(content);

        List<ChatParticipation> newParticipations = associatedChat.getParticipations().stream().
                peek(participation -> {
            //For the participation of the sender of the message in this chat,
            // we only update the last message of the participation
            if (sender.equals(participation.getParticipatingUser()) && !participation.isMuted())
            {
                participation.setLastMessage(content);
            }
            //For participations that do not belong to the sender, we increase their unread count and
            // update their last message only if the chat has not been muted
            else
            {
                if (!participation.isMuted())
                {
                    participation.setLastMessage(content);
                    participation.setUnreadMessagesCount(participation.getUnreadMessagesCount() + 1);
                }
            }

        }).collect(Collectors.toCollection(ArrayList::new));
        associatedChat.setParticipations(newParticipations);

        Message savedMessage = messageRepository.save(newMessage);

        websocketsService.sendNewMessageNotification(associatedChat, sender, savedMessage);

        return new NewMessageDTO(chatRepository.save(associatedChat), savedMessage);
    }


    public NewMessageDTO createMessage(User sender, UUID receivingObjectId, String messageContent)
    {
        Optional<User> recipient = userRepository.findById(receivingObjectId);

        //If we find a user with the given id, the recipient will receive a message from this user for the first time
        //So, create a private chat between the 2 users and send the message
        if (recipient.isPresent())
        {
           User receivingUser = recipient.get();

            // If the recipient has not selected the setting, just send the message with the content,
            // creating a chat between the users first
            if(!receivingUser.getUserProfile().isMessagesFromFriendsOnly())
            {
                return sendMessageTo(sender, receivingUser, messageContent);
            }

            //Otherwise, only send a message if the 2 users are friends with each other.
            Optional<FriendRelationship> foundRelationship =
                    friendRelationshipRepository.findByTwoUsers(sender, receivingUser);
            if (foundRelationship.isPresent()) {
                return sendMessageTo(sender, receivingUser, messageContent);
            }
        }

        // If the provided identifier is instead the id of a chat,
        // then send a message to the chat identified by it.
        Optional<AbstractChat> recipientChat = chatRepository.findByIdWithParticipations(receivingObjectId);

        return recipientChat.map(chat ->
                sendMessageTo(sender, messageContent, chat)).orElse(null);

    }

    public List<DisplayChatResponse> displayAllChats(User displayingUser)
    {
        List<ChatParticipation> allChatParticipations = participationRepository.findByParticipatingUser(displayingUser);

        // We have to use the collect method at the very end,
        // because using toList() returns an unmodifiable list.

        return allChatParticipations.stream()
                .map(participation -> {
                    AbstractChat participatedChat = participation.getParticipatedChat();

                    int unreadMessageCount = chatUtilService.determineUnreadMessageCount(participatedChat, displayingUser);

                    String chatDisplayName = chatUtilService.determineChatDisplayName(participatedChat, displayingUser);

                    String chatPictureUrl = chatUtilService.determineChatPictureUrl(participatedChat, displayingUser);

                    return new DisplayChatResponse(participatedChat.getId(), chatDisplayName,
                            chatPictureUrl, Objects.requireNonNullElse(participation.getLastMessage(),
                            participatedChat.getLastMessage()),
                            unreadMessageCount, participation.isMuted());
                }).collect(Collectors.toCollection(ArrayList::new));
    }

    public LoadChatResponse loadChat(UUID chatId, User loadingUser)
    {
        AbstractChat chat = chatRepository.findByIdWithMessages(chatId).orElseThrow(
                () -> new NotFoundException("Chat with this id not found")
        );
        String chatDisplayName = chatUtilService.determineChatDisplayName(chat, loadingUser);
        String chatPictureUrl = chatUtilService.determineChatPictureUrl(chat, loadingUser);

        List<MessageDisplayDTO> messages = chat.getMessages()
                .stream().
                map(message -> {
                    boolean displayOnTheRight = message.getSender().equals(loadingUser);
                    return new MessageDisplayDTO(message.getDisplayedContent(),
                            message.getCreatedAt(),
                            displayOnTheRight);
                }).toList();

        return new LoadChatResponse(chatDisplayName, chat.getId(), messages, chatPictureUrl);
    }


    public List<UUID> findAllChatIdsByUser(User user)
    {
        List<UUID> groupChatIds = participationRepository.findIdsByParticipatingUser(user);

        List<UUID> privateChatIds = chatRepository.findIdsByUser(user);

        //Just add up the 2 lists together.
        return Stream.of(groupChatIds, privateChatIds).flatMap(Collection::stream).
                toList();
    }

    public boolean toggleChatMute(User togglingUser, UUID chatId)
    {
        AbstractChat foundChat = chatRepository.findById(chatId).orElseThrow(
                () -> new NotFoundException("A chat with the specified id does not exist")
        );

        ChatParticipation foundParticipation = participationRepository.
                findByParticipatingUserAndParticipatedChat(togglingUser, foundChat).
                orElseThrow(() -> new BelongingException("You cannot mute or unmute a chat you are not participating in"));

        boolean muted = foundParticipation.isMuted();

        foundParticipation.setMuted(!muted);
        participationRepository.save(foundParticipation);

        return !muted;

    }
}

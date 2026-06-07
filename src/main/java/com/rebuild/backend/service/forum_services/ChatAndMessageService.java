package com.rebuild.backend.service.forum_services;

import com.rebuild.backend.model.dtos.forum_dtos.MessageDisplayDTO;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.*;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.entities.util_entitites.base_entities.AbstractChat;
import com.rebuild.backend.repository.messaging_and_friendship_repositories.*;
import com.rebuild.backend.utils.exceptions.ApiException;
import com.rebuild.backend.utils.exceptions.BelongingException;
import com.rebuild.backend.utils.exceptions.ChatException;
import com.rebuild.backend.utils.exceptions.NotFoundException;
import com.rebuild.backend.model.responses.forum_responses.DisplayChatResponse;
import com.rebuild.backend.model.responses.forum_responses.LoadChatResponse;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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

    @Transactional
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

    @Transactional
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

    private MessageDisplayDTO sendMessageTo(User sender, User recipient, String messageContent){
        //If we enter this method, we know that we will have to create a new chat.
        PrivateChat createdChat = new PrivateChat(sender, recipient, messageContent);

        Message newMessage = new Message(sender, messageContent);
        newMessage.setAssociatedChat(createdChat);
        createdChat.getMessages().add(newMessage);
        createdChat.setLastMessage(messageContent);

        //Here, we do need to save both the message and the chat, since the chat itself is also a brand-new entity
        chatRepository.save(createdChat);
        Message savedMessage = messageRepository.save(newMessage);

        websocketsService.sendNewChatNotification(createdChat, sender, savedMessage, recipient);

        return savedMessage.toDTo(true);
    }

    private MessageDisplayDTO sendMessageTo(User sender, String content,
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

        return savedMessage.toDTo(true);
    }


    @Transactional
    public MessageDisplayDTO createMessage(User sender, UUID receivingObjectId, String messageContent)
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
                sendMessageTo(sender, messageContent, chat)).
                orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "You are not authorized to send messages to this user or channel"));

    }

    @Transactional
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

    @Transactional
    public LoadChatResponse loadChat(UUID chatId, User loadingUser, int pageNumber)
    {
        if (pageNumber < 0)
        {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Page number must be greater than or equal to 0.");
        }
        AbstractChat chat = chatRepository.findByIdWithMessages(chatId).orElseThrow(
                () -> new NotFoundException("Chat with this id not found")
        );

        ChatParticipation userParticipation = participationRepository.
                findByParticipatingUserAndParticipatedChat(loadingUser, chat).
                orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "You are not participating in this chat, you can't load it"));
        //Update the participation of this user in this chat.
        userParticipation.setUnreadMessagesCount(0);
        userParticipation.setLastMessage(chat.getLastMessage());
        participationRepository.save(userParticipation);

        String chatDisplayName = chatUtilService.determineChatDisplayName(chat, loadingUser);
        String chatPictureUrl = chatUtilService.determineChatPictureUrl(chat, loadingUser);

        Pageable request = PageRequest.of(pageNumber, 30, Sort.by(Sort.Direction.DESC, "createdAt"));

        Slice<Message> currentMessages = messageRepository.findByAssociatedChat(chat, request);

        List<MessageDisplayDTO> messages = currentMessages.getContent()
                .stream().
                map(message -> {
                    boolean displayOnTheRight = message.getSender().equals(loadingUser);
                    return message.toDTo(displayOnTheRight);
                }).toList();

        return new LoadChatResponse(chatDisplayName, chat.getId(), messages,
                chatPictureUrl, currentMessages.hasNext());
    }


    public List<UUID> findAllChatIdsByUser(User user)
    {
        return chatRepository.findIdsByUser(user);
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

        //Return the new mute status of the chat
        return !muted;
    }


    public MessageDisplayDTO removeMessage(User removingUser, UUID messageId)
    {
        Message foundMessage = messageRepository.findByIdAndSender(messageId, removingUser).
                orElseThrow(() -> new BelongingException("Message with this id either does not exist or " +
                        "does not belong to you"));

        foundMessage.setRemoved(true);

        Message savedMessage = messageRepository.save(foundMessage);

        // The message should always be displayed on the right,
        // because the user can only remove their own messages anyway
        return savedMessage.toDTo(true);
    }
}

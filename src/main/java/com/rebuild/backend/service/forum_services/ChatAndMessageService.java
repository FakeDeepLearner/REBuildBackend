package com.rebuild.backend.service.forum_services;

import com.rebuild.backend.model.dtos.forum_dtos.MessageDisplayDTO;
import com.rebuild.backend.model.dtos.forum_dtos.NewMessageDTO;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.*;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.exceptions.BelongingException;
import com.rebuild.backend.model.exceptions.ChatException;
import com.rebuild.backend.model.exceptions.NotFoundException;
import com.rebuild.backend.model.responses.DisplayChatResponse;
import com.rebuild.backend.model.responses.LoadChatResponse;
import com.rebuild.backend.repository.messaging_and_friendship_repositories.ChatInvitationRepository;
import com.rebuild.backend.repository.messaging_and_friendship_repositories.ChatParticipationRepository;
import com.rebuild.backend.repository.messaging_and_friendship_repositories.ChatRepository;
import com.rebuild.backend.repository.messaging_and_friendship_repositories.FriendRelationshipRepository;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatAndMessageService {

    private final ChatRepository chatRepository;

    private final UserRepository userRepository;

    private final FriendRelationshipRepository friendRelationshipRepository;

    private final ChatParticipationRepository participationRepository;

    private final ChatInvitationRepository chatInvitationRepository;

    private final ChatUtilService chatUtilService;

    @Autowired
    public ChatAndMessageService(ChatRepository chatRepository, UserRepository userRepository,
                                 FriendRelationshipRepository friendRelationshipRepository,
                                 ChatParticipationRepository participationRepository,
                                 ChatInvitationRepository chatInvitationRepository, ChatUtilService chatUtilService) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.friendRelationshipRepository = friendRelationshipRepository;
        this.participationRepository = participationRepository;
        this.chatInvitationRepository = chatInvitationRepository;
        this.chatUtilService = chatUtilService;
    }


    public GroupChat createNewGroupChat(User creatingUser, String chatName)
    {
        GroupChat newChat = new GroupChat();

        ChatParticipation userParticipation = new ChatParticipation(creatingUser, newChat, true);
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
                associatedChat, false);

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

        return chatInvitationRepository.save(newInvitation);
    }


    private PrivateChat createChatBetween(User sender, User recipient)
    {
        PrivateChat newChat = new PrivateChat(sender, recipient);
        
        return chatRepository.save(newChat);
    }

    private NewMessageDTO sendMessageTo(User sender, User recipient, String messageContent){
        //If we enter this method, we know that we will have to create a new chat.
        PrivateChat createdChat = createChatBetween(sender, recipient);

        Message newMessage = new Message(sender, messageContent);
        newMessage.setAssociatedChat(createdChat);
        createdChat.getMessages().add(newMessage);
        createdChat.setLastMessage(messageContent);
        
        createdChat.addRecipientUnread();

        return new NewMessageDTO(chatRepository.save(createdChat), newMessage);
    }

    private NewMessageDTO sendMessageTo(User sender, String content,
                                        AbstractChat associatedChat)
    {

        Message newMessage = new Message(sender, content);
        newMessage.setAssociatedChat(associatedChat);
        associatedChat.getMessages().add(newMessage);
        associatedChat.setLastMessage(content);
        
        if (associatedChat instanceof GroupChat groupChat)
        {
            List<ChatParticipation> otherChatParticipations = groupChat.getParticipations().stream().
                    filter(participation -> !sender.equals(participation.getParticipatingUser())).
                    collect(Collectors.toCollection(ArrayList::new));

            // For every other user participating in this chat except for the
            // sender of the message, they will have 1 more unread message
            otherChatParticipations.forEach(participation ->
                    participation.setUnreadMessagesCount(participation.getUnreadMessagesCount() + 1));
        }

        if (associatedChat instanceof PrivateChat privateChat)
        {
            User otherUser = chatUtilService.determineOtherChatUser(privateChat, sender);
            if (otherUser.equals(sender))
            {
                privateChat.addSenderUnread();
            }
            else {
                privateChat.addRecipientUnread();
            }
        }

        

        return new NewMessageDTO(chatRepository.save(associatedChat), newMessage);
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


            Optional<FriendRelationship> foundRelationship =
                    friendRelationshipRepository.findByTwoUsers(sender, receivingUser);
            if (foundRelationship.isPresent()) {
                return sendMessageTo(sender, receivingUser, messageContent);
            }
        }

        // If the provided identifier is instead the id of a chat,
        // then send a message to the chat identified by it.
        Optional<AbstractChat> recipientChat = chatRepository.findById(receivingObjectId);

        return recipientChat.map(chat ->
                sendMessageTo(sender, messageContent, chat)).orElse(null);

    }

    public List<DisplayChatResponse> displayAllChats(User displayingUser)
    {
        List<ChatParticipation> groupChatParticipation = participationRepository.findParticipationsByUser(displayingUser);
        List<PrivateChat> userChats = chatRepository.findPrivateChatsByUser(displayingUser);

        List<DisplayChatResponse> privateChatResponses = userChats.stream()
                .map(abstractChat -> {
                    int unreadMessageCount = chatUtilService.determineUnreadMessageCount(abstractChat, displayingUser);

                    String chatDisplayName = chatUtilService.determineChatDisplayName(abstractChat, displayingUser);

                    String chatPictureUrl = chatUtilService.determineChatPictureUrl(abstractChat, displayingUser);

                    return new DisplayChatResponse(abstractChat.getId(), chatDisplayName,
                            chatPictureUrl, abstractChat.getLastMessage(),
                            unreadMessageCount);
                }).toList();

        //We have to use the collect method at the very end, because using toList() returns an unmodifiable list.
        List<DisplayChatResponse> groupChatResponses = groupChatParticipation.stream()
                .map(participation -> {
                    GroupChat participatedChat = participation.getParticipatedChat();

                    int unreadMessageCount = chatUtilService.determineUnreadMessageCount(participatedChat, displayingUser);

                    String chatDisplayName = chatUtilService.determineChatDisplayName(participatedChat, displayingUser);

                    String chatPictureUrl = chatUtilService.determineChatPictureUrl(participatedChat, displayingUser);

                    return new DisplayChatResponse(participatedChat.getId(), chatDisplayName,
                            chatPictureUrl, participatedChat.getLastMessage(),
                            unreadMessageCount);
                }).collect(Collectors.toCollection(ArrayList::new));

        groupChatResponses.addAll(privateChatResponses);

        return groupChatResponses;


    }



    public LoadChatResponse loadChat(UUID chatId, User loadingUser)
    {
        AbstractChat chat = chatRepository.findByIdWithMessages(chatId).orElseThrow(
                () -> new NotFoundException("Chat with this id not found")
        );
        String chatDisplay = chatUtilService.determineChatDisplayName(chat, loadingUser);
        String chatPictureUrl = chatUtilService.determineChatPictureUrl(chat, loadingUser);

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

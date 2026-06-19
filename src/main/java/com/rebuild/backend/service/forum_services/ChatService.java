package com.rebuild.backend.service.forum_services;

import com.rebuild.backend.model.dtos.forum_dtos.MessageDisplayDTO;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.ChatInvitation;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.ChatParticipation;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.GroupChat;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.Message;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.entities.util_entitites.base_entities.AbstractChat;
import com.rebuild.backend.model.responses.forum_responses.DisplayChatResponse;
import com.rebuild.backend.model.responses.forum_responses.LoadChatResponse;
import com.rebuild.backend.repository.messaging_and_friendship_repositories.ChatInvitationRepository;
import com.rebuild.backend.repository.messaging_and_friendship_repositories.ChatParticipationRepository;
import com.rebuild.backend.repository.messaging_and_friendship_repositories.ChatRepository;
import com.rebuild.backend.repository.messaging_and_friendship_repositories.MessageRepository;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import com.rebuild.backend.utils.exceptions.ApiException;
import com.rebuild.backend.utils.exceptions.BelongingException;
import com.rebuild.backend.utils.exceptions.ChatException;
import com.rebuild.backend.utils.exceptions.NotFoundException;
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
public class ChatService {

    private final ChatRepository chatRepository;

    private final ChatInvitationRepository chatInvitationRepository;

    private final UserRepository userRepository;

    private final ChatUtilService chatUtilService;

    private final WebsocketsService websocketsService;

    private final ChatParticipationRepository participationRepository;

    private final MessageRepository messageRepository;

    @Autowired
    public ChatService(ChatRepository chatRepository,
                       ChatInvitationRepository chatInvitationRepository,
                       UserRepository userRepository, ChatUtilService chatUtilService,
                       WebsocketsService websocketsService,
                       ChatParticipationRepository participationRepository, MessageRepository messageRepository) {
        this.chatRepository = chatRepository;
        this.chatInvitationRepository = chatInvitationRepository;
        this.userRepository = userRepository;
        this.chatUtilService = chatUtilService;
        this.websocketsService = websocketsService;
        this.participationRepository = participationRepository;
        this.messageRepository = messageRepository;
    }


    @Transactional
    public GroupChat createNewGroupChat(User creatingUser, String chatName)
    {
        GroupChat newChat = new GroupChat();

        ChatParticipation userParticipation = new ChatParticipation(creatingUser, newChat, true,
                true);
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
                associatedChat, false, false);
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

        //If this user is not an administrator in this chat, they can't send any invites
        if (chatUtilService.userIsNotAdminInChat(foundChat, sender))
        {
            throw new ChatException(HttpStatus.UNAUTHORIZED, "You can't invite anyone to this chat " +
                    "because you are not an administrator in this chat.");
        }

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

    @Transactional
    public boolean toggleUserAdmin(User administratingUser, UUID chatId, UUID userId)
    {
        AbstractChat foundChat = chatRepository.findById(chatId).orElseThrow(() -> new NotFoundException(
                "Chat with the specified id not found"
        ));

        if (!(foundChat instanceof GroupChat))
        {
            throw new ChatException(HttpStatus.FORBIDDEN,
                    "This chat is not a group chat, that operation cannot be done");
        }

        ChatParticipation recipientParticipation = participationRepository.
                findByParticipatingUser_IdAndParticipatedChat(userId, foundChat).orElseThrow(()
                        -> new NotFoundException("User with this ID is not found, or is not a member of this chat"));

        if (chatUtilService.userIsNotAdminInChat(foundChat, administratingUser))
        {
            throw new ChatException(HttpStatus.UNAUTHORIZED, "Only administrators are able to do this operation");
        }

        if (recipientParticipation.getIsGroupOwner())
        {
            throw new ChatException(HttpStatus.FORBIDDEN, "The owner of the chat must remain an administrator");
        }

        boolean currentStatus = recipientParticipation.getIsAdmin();

        recipientParticipation.setIsAdmin(!currentStatus);

        ChatParticipation savedParticipation = participationRepository.save(recipientParticipation);

        return savedParticipation.getIsAdmin();

    }

    @Transactional
    public void kickUserFromChat(User kickingUser, UUID chatId, UUID userId)
    {
        AbstractChat foundChat = chatRepository.findById(chatId).orElseThrow(() -> new NotFoundException(
                "Chat with the specified id not found"
        ));

        if (!(foundChat instanceof GroupChat))
        {
            throw new ChatException(HttpStatus.FORBIDDEN,
                    "This chat is not a group chat, that operation cannot be done");
        }

        ChatParticipation userParticipation =
                participationRepository.findByParticipatingUserAndParticipatedChat(kickingUser, foundChat)
                        .orElseThrow(() -> new NotFoundException("You are not a member in this chat"));

        ChatParticipation recipientParticipation = participationRepository.
                findByParticipatingUser_IdAndParticipatedChat(userId, foundChat).orElseThrow(()
                        -> new NotFoundException("User with this ID is not found, or is not a member of this chat"));


        if(userParticipation.hasNoAdminPrivileges())
        {
            throw new ChatException(HttpStatus.UNAUTHORIZED, "Only administrators or owners can kick users");
        }

        if (recipientParticipation.getIsGroupOwner()) {
            throw new ChatException(HttpStatus.FORBIDDEN, "The group owner cannot be kicked");
        }

        foundChat.getParticipations().remove(userParticipation);

        chatRepository.save(foundChat);

        //Once again, this is a safe cast, since we know that the chat cannot be a private chat by the time we get here.
        websocketsService.sendKickNotification((GroupChat) foundChat, recipientParticipation);
    }

    @Transactional
    public void deleteChat(User deletingUser, UUID chatId)
    {
        AbstractChat foundChat = chatRepository.findById(chatId).orElseThrow(() -> new NotFoundException(
                "Chat with the specified id not found"
        ));

        if (!(foundChat instanceof GroupChat))
        {
            throw new ChatException(HttpStatus.FORBIDDEN,
                    "This chat is not a group chat, that operation cannot be done");
        }

        ChatParticipation userParticipation =
                participationRepository.findByParticipatingUserAndParticipatedChat(deletingUser, foundChat)
                        .orElseThrow(() -> new NotFoundException("You are not a member in this chat"));

        if (!userParticipation.getIsGroupOwner())
        {
            throw new ChatException(HttpStatus.FORBIDDEN, "Only the group owner can disband the group");
        }

        //This will automatically delete all the messages and participations as well, because of orphan removal
        foundChat.setMessages(null);
        foundChat.setParticipations(null);
        chatRepository.delete(foundChat);
    }

    @Transactional
    public void leaveChat(User leavingUser, UUID chatId)
    {
        AbstractChat foundChat = chatRepository.findById(chatId).orElseThrow(() -> new NotFoundException(
                "Chat with the specified id not found"
        ));

        if (!(foundChat instanceof GroupChat))
        {
            throw new ChatException(HttpStatus.FORBIDDEN,
                    "This chat is not a group chat, that operation cannot be done");
        }

        ChatParticipation userParticipation =
                participationRepository.findByParticipatingUserAndParticipatedChat(leavingUser, foundChat)
                        .orElseThrow(() -> new NotFoundException("You are not a member in this chat"));

        if (userParticipation.getIsGroupOwner())
        {
            throw new ChatException(HttpStatus.FORBIDDEN, "You must transfer ownership to another user before leaving");
        }

        foundChat.getParticipations().remove(userParticipation);
        leavingUser.getChatParticipations().remove(userParticipation);
        participationRepository.delete(userParticipation);
    }

    @Transactional
    public void transferChatOwnership(User transferingUser, UUID chatId, UUID userId) {
        AbstractChat foundChat = chatRepository.findById(chatId).orElseThrow(() -> new NotFoundException(
                "Chat with the specified id not found"
        ));

        if (!(foundChat instanceof GroupChat)) {
            throw new ChatException(HttpStatus.FORBIDDEN,
                    "This chat is not a group chat, that operation cannot be done");
        }

        ChatParticipation userParticipation =
                participationRepository.findByParticipatingUserAndParticipatedChat(transferingUser, foundChat)
                        .orElseThrow(() -> new NotFoundException("You are not a member in this chat"));

        ChatParticipation recipientParticipation = participationRepository.
                findByParticipatingUser_IdAndParticipatedChat(userId, foundChat).orElseThrow(()
                        -> new NotFoundException("User with this ID is not found, or is not a member of this chat"));

        if (!userParticipation.getIsGroupOwner())
        {
            throw new ChatException(HttpStatus.UNAUTHORIZED, "You are not the owner of this chat");
        }

        // The user becomes the owner, and thus an admin, and the user that transfers ownership loses it
        // but still retains being an admin
        recipientParticipation.setIsGroupOwner(true);
        recipientParticipation.setIsAdmin(true);
        userParticipation.setIsGroupOwner(false);

        participationRepository.save(userParticipation);
        participationRepository.save(recipientParticipation);
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
                map(message -> message.toDTo(loadingUser)).toList();

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
}

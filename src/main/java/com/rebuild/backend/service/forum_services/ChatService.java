package com.rebuild.backend.service.forum_services;

import com.rebuild.backend.model.dtos.forum_dtos.message_and_chat_dtos.ChatUserDisplayDTO;
import com.rebuild.backend.model.dtos.forum_dtos.message_and_chat_dtos.MessageDisplayDTO;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.*;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.entities.util_entitites.AbstractChat;
import com.rebuild.backend.model.forms.chat_forms.NewChatForm;
import com.rebuild.backend.model.responses.forum_responses.DisplayChatResponse;
import com.rebuild.backend.model.responses.forum_responses.LoadChatResponse;
import com.rebuild.backend.model.responses.forum_responses.LoadChatUsersResponse;
import com.rebuild.backend.repository.messaging_and_friendship_repositories.*;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import com.rebuild.backend.utils.UserPair;
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
@Transactional
public class ChatService {

    private final ChatRepository chatRepository;

    private final ChatInvitationRepository chatInvitationRepository;

    private final ChatUtilService chatUtilService;

    private final ChatParticipationRepository participationRepository;

    private final MessageRepository messageRepository;

    private final UserRepository userRepository;

    private final FriendshipRepository friendshipRepository;

    @Autowired
    public ChatService(ChatRepository chatRepository,
                       ChatInvitationRepository chatInvitationRepository,
                       ChatUtilService chatUtilService, ChatParticipationRepository participationRepository,
                       MessageRepository messageRepository, UserRepository userRepository, FriendshipRepository friendshipRepository) {
        this.chatRepository = chatRepository;
        this.chatInvitationRepository = chatInvitationRepository;
        this.chatUtilService = chatUtilService;
        this.participationRepository = participationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
    }

    
    public GroupChat createNewGroupChat(User creatingUser, NewChatForm newChatForm)
    {
        GroupChat newChat = new GroupChat(newChatForm);

        ChatParticipation userParticipation = new ChatParticipation(creatingUser, newChat, true);
        creatingUser.addChatParticipation(userParticipation);

        newChat.setParticipations(new ArrayList<>(List.of(userParticipation)));

        List<ChatInvitation> createdChatInvitations = newChatForm.invitedUserIds().stream().map(
                userId -> createInvitationToNewChat(creatingUser, newChat,
                        userId)
        ).collect(Collectors.toCollection(ArrayList::new));

        newChat.setInvitations(createdChatInvitations);

        return chatRepository.save(newChat);
    }
    
    public GroupChat acceptChatInvitation(User recipient, UUID invitationId)
    {
        ChatInvitation foundInvitation = chatInvitationRepository.findByIdAndRecipient(invitationId,
                recipient).orElseThrow(() ->
                new BelongingException("This invitation either does not exist or does not belong to you"));

        GroupChat associatedChat = foundInvitation.getAssociatedChat();

        ChatParticipation recipientParticipation = new ChatParticipation(recipient,
                associatedChat, false);
        recipientParticipation.setLastMessage(associatedChat.getLastMessage());

        associatedChat.setMemberCount(associatedChat.getMemberCount() + 1);
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
    
    public void leaveChat(User leavingUser, UUID chatId)
    {

        ChatParticipation leavingUserParticipation = participationRepository.findByChatIdAndUser(
                chatId, leavingUser
        ).orElseThrow(() -> new ChatException(HttpStatus.NOT_FOUND, "The chat with this id either does not exist," +
                "or you are not a member in this chat."));

        AbstractChat chat = leavingUserParticipation.getParticipatedChat();

        if (!(chat instanceof GroupChat))
        {
            throw new ChatException(HttpStatus.FORBIDDEN, "This action can only be done on group chats");
        }

        //If this user is the only administrator and there are members other than this user, they can't leave.
        if (leavingUserParticipation.getIsAdmin() && chat.getAdministratorCount() == 1 && chat.getMemberCount() > 1)
        {
            throw new ApiException(HttpStatus.FORBIDDEN, "You cannot leave this chat as the only administrator. " +
                    "Please make another user an administrator if you want to leave.");
        }


        chat.setMemberCount(chat.getMemberCount() - 1);
        chat.getParticipations().remove(leavingUserParticipation);
        leavingUser.getChatParticipations().remove(leavingUserParticipation);
        participationRepository.delete(leavingUserParticipation);

        //If the leaving user was the last member of the chat, then delete the chat as well.
        if (chat.getMemberCount() == 0)
        {
            chatRepository.delete(chat);
        }

        userRepository.save(leavingUser);
    }

    public List<DisplayChatResponse> displayAllChats(User displayingUser)
    {
        List<ChatParticipation> allChatParticipations = participationRepository.
                findByParticipatingUser(displayingUser);

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

        Slice<Message> currentMessages = messageRepository.findByAssociatedChat_Id(chatId, request);

        List<MessageDisplayDTO> messages = currentMessages.getContent()
                .stream().
                map(message -> message.toDTo(loadingUser)).toList();

        return new LoadChatResponse(chatDisplayName, chat.getId(), messages,
                chatPictureUrl, currentMessages.hasNext(), userParticipation.getIsAdmin());
    }

    public List<UUID> findAllChatIdsByUser(User user)
    {
        return chatRepository.findIdsByUser(user);
    }

    public boolean toggleChatMute(User togglingUser, UUID chatId)
    {
        ChatParticipation foundParticipation = participationRepository.
                findByChatIdAndUser(chatId, togglingUser).
                orElseThrow(() -> new BelongingException("You cannot mute or unmute a chat you are not participating in"));

        boolean muted = foundParticipation.isMuted();

        foundParticipation.setMuted(!muted);
        participationRepository.save(foundParticipation);

        //Return the new mute status of the chat
        return !muted;
    }

    
    public LoadChatUsersResponse loadChatUsers(User loadingUser, UUID chatId)
    {
        ChatParticipation loadingUserParticipation = participationRepository.
                findByChatIdAndUserWithParticipations(chatId, loadingUser).orElseThrow(
                        () -> new ChatException(HttpStatus.NOT_FOUND, "A chat with this id does not exist," +
                                "or you are not a member of this chat")
                );

        AbstractChat userChat = loadingUserParticipation.getParticipatedChat();
        List<ChatParticipation> allParticipations = userChat.getParticipations();
        List<ChatUserDisplayDTO> userDisplayDTOS = allParticipations.stream().map(
                chatParticipation -> {
                    User participatingUser = chatParticipation.getParticipatingUser();
                    return new ChatUserDisplayDTO(participatingUser.getId(),
                            participatingUser.getForumUsername(), chatParticipation.getCreatedAt(),
                            chatParticipation.getIsAdmin(),
                            participatingUser.equals(loadingUser), participatingUser.getImageUrl());
                }
        ).toList();

        return new LoadChatUsersResponse(userDisplayDTOS, userChat.getMemberCount(),
                loadingUserParticipation.getIsAdmin());
    }

    private ChatInvitation createInvitationToNewChat(User sender, GroupChat newGroupChat, UUID invitedUserId)
    {
        User recipient = userRepository.findById(invitedUserId).orElseThrow(
                () -> new NotFoundException("User with this id is not found.")
        );

        UserPair userPair = new UserPair(recipient, sender);
        boolean usersAreFriends = friendshipRepository.existsByLowUserIdAndHighUserId(
                userPair.lowId(),
                userPair.highId()
        );

        if (!usersAreFriends)
        {
            throw new ApiException(HttpStatus.FORBIDDEN, "You cannot invite users who you are not " +
                    "friends with to the chat while creating it");
        }

        return new ChatInvitation(sender, recipient, newGroupChat);
    }
}

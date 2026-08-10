package com.rebuild.backend.service.chat_services;

import com.rebuild.backend.model.dtos.forum_dtos.message_and_chat_dtos.ChatUserDisplayDTO;
import com.rebuild.backend.model.entities.chat_entities.*;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.entities.util_entitites.AbstractChat;
import com.rebuild.backend.model.enums.ChatStatus;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatService {

    private final ChatInvitationRepository chatInvitationRepository;

    private final ChatUtilService chatUtilService;

    private final ChatParticipationRepository participationRepository;

    private final UserRepository userRepository;

    private final FriendshipRepository friendshipRepository;

    private final ChatApplicationRepository chatApplicationRepository;

    private final PrivateChatRepository privateChatRepository;

    private final GroupChatRepository groupChatRepository;

    @Autowired
    public ChatService(ChatInvitationRepository chatInvitationRepository,
                       ChatUtilService chatUtilService, ChatParticipationRepository participationRepository,
                       UserRepository userRepository, FriendshipRepository friendshipRepository,
                       ChatApplicationRepository chatApplicationRepository, PrivateChatRepository privateChatRepository, GroupChatRepository groupChatRepository) {
        this.chatInvitationRepository = chatInvitationRepository;
        this.chatUtilService = chatUtilService;
        this.participationRepository = participationRepository;
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.chatApplicationRepository = chatApplicationRepository;
        this.privateChatRepository = privateChatRepository;
        this.groupChatRepository = groupChatRepository;
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

        return groupChatRepository.save(newChat);
    }
    
    public GroupChat acceptChatInvitation(User recipient, UUID invitationId)
    {
        ChatInvitation foundInvitation = chatInvitationRepository.findByIdAndRecipient(invitationId,
                recipient).orElseThrow(() ->
                new BelongingException("This invitation either does not exist or has not been addressed to you."));

        GroupChat associatedChat = foundInvitation.getAssociatedChat();

        ChatParticipation newParticipation = chatUtilService.addUserToChat(associatedChat, recipient);
        participationRepository.save(newParticipation);

        chatInvitationRepository.delete(foundInvitation);

        return groupChatRepository.save(associatedChat);

    }

    public void acceptAllChatInvitations(User user)
    {
        List<ChatInvitation> invitations = chatInvitationRepository.findByRecipient(user);


        List<ChatParticipation> participations = invitations.stream().map(
                chatInvitation -> {
                    GroupChat associatedChat = chatInvitation.getAssociatedChat();
                    return chatUtilService.addUserToChat(associatedChat, user);
                }
        ).collect(Collectors.toCollection(ArrayList::new));

        participationRepository.saveAll(participations);
    }
    
    public void declineChatInvitation(User recipient, UUID invitationId)
    {
        ChatInvitation foundInvitation = chatInvitationRepository.findByIdAndRecipient(invitationId,
                recipient).orElseThrow(() ->
                new BelongingException("This invitation either does not exist or does not belong to you"));

        chatInvitationRepository.delete(foundInvitation);
    }

    public void declineAllChatInvitations(User user)
    {
        List<ChatInvitation> invitations = chatInvitationRepository.findByRecipient(user);

        chatInvitationRepository.deleteAll(invitations);
    }
    
    public void leaveChat(User leavingUser, UUID chatId)
    {

        ChatParticipation leavingUserParticipation = participationRepository.findByChatIdAndUser(
                chatId, leavingUser
        ).orElseThrow(() -> new ChatException(HttpStatus.NOT_FOUND, "The chat with this id either does not exist," +
                "or you are not a member in this chat."));

        AbstractChat chat = leavingUserParticipation.getAssociatedChat();

        if (!(chat instanceof GroupChat groupChat))
        {
            throw new ChatException(HttpStatus.FORBIDDEN, "This action can only be done on group chats");
        }

        //If this user is the only administrator and there are members other than this user, they can't leave.
        if (leavingUserParticipation.getIsAdmin() && chat.getAdministratorCount() == 1 && chat.getMemberCount() > 1)
        {
            throw new ApiException(HttpStatus.FORBIDDEN, "You cannot leave this chat as the only administrator. " +
                    "Please make another user an administrator if you want to leave.");
        }

        chatUtilService.removeUserFromChat(groupChat, leavingUserParticipation);

        //If the leaving user was the last member of the chat, then delete the chat as well.
        if (groupChat.getMemberCount() == 0)
        {
            groupChatRepository.delete(groupChat);
        }

        userRepository.save(leavingUser);
    }


    public List<DisplayChatResponse> displayAllPrivateChats(User displayingUser)
    {
        List<ChatParticipation> privateParticipations = participationRepository.
                findPrivateParticipationsParticipatingUser(displayingUser);

        return chatUtilService.displayChatResponses(privateParticipations, displayingUser);
    }

    public List<DisplayChatResponse> displayAllGroupChats(User displayingUser)
    {
        List<ChatParticipation> privateParticipations = participationRepository.
                findGroupParticipationsParticipatingUser(displayingUser);

        return chatUtilService.displayChatResponses(privateParticipations, displayingUser);
    }

    public LoadChatResponse loadPrivateChat(UUID chatId, User loadingUser, int pageNumber)
    {
        if (pageNumber < 0)
        {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Page number must be greater than or equal to 0.");
        }
        PrivateChat chat = privateChatRepository.findById(chatId).orElseThrow(
                () -> new NotFoundException("Private chat with this id is not found")
        );
        return chatUtilService.loadChat(chat, loadingUser, pageNumber);
    }

    public LoadChatResponse loadGroupChat(UUID chatID, User loadingUser, int pageNumber)
    {
        if (pageNumber < 0)
        {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Page number must be greater than or equal to 0.");
        }
        GroupChat chat = groupChatRepository.findById(chatID).orElseThrow(
                () -> new NotFoundException("Group chat with this id is not found")
        );
        return chatUtilService.loadChat(chat, loadingUser, pageNumber);
    }

    public List<UUID> findAllChatIdsByUser(User user)
    {
        return participationRepository.findIdsByUser(user);
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

        AbstractChat userChat = loadingUserParticipation.getAssociatedChat();
        List<ChatParticipation> allParticipations = userChat.getParticipations();
        List<ChatUserDisplayDTO> userDisplayDTOS = allParticipations.stream().map(
                chatParticipation -> {
                    User participatingUser = chatParticipation.getParticipatingUser();
                    return new ChatUserDisplayDTO(participatingUser.getId(),
                            participatingUser.getForumUsername(), chatParticipation.getCreatedAt().toString(),
                            chatParticipation.getIsAdmin(),
                            participatingUser.equals(loadingUser), participatingUser.getImageUrl());
                }
        ).toList();

        return new LoadChatUsersResponse(userDisplayDTOS, userChat.getMemberCount(),
                userChat instanceof GroupChat gc ? gc.getChatStatus().value : null,
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

    public void applyToJoinChat(User applyingUser, UUID chatId, String content)
    {
        if (content.isBlank())
        {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Content cannot be empty.");
        }
        GroupChat foundChat = groupChatRepository.findById(chatId).orElseThrow(
                () -> new NotFoundException("Group with this id is not found.")
        );

        //A group chat that is closed cannot receive applications
        if (foundChat.getChatStatus().equals(ChatStatus.CLOSED))
        {
            throw new ApiException(HttpStatus.FORBIDDEN, "This chat is closed, it cannot receive any applications");
        }

        boolean userApplicationExists = chatApplicationRepository.existsByAssociatedChatAndAssociatedUser(foundChat,
                applyingUser);

        if (userApplicationExists)
        {
            throw new ApiException(HttpStatus.CONFLICT, "You already have an application to this chat.");
        }

        JoinChatApplication joinChatApplication = new JoinChatApplication(content, applyingUser, foundChat);
        applyingUser.addChatApplication(joinChatApplication);
        foundChat.getApplications().add(joinChatApplication);
        chatApplicationRepository.save(joinChatApplication);
    }

    public void cancelChatApplication(User cancellingUser, UUID applicationId)
    {
        JoinChatApplication foundApplication = chatApplicationRepository.findById(applicationId).orElseThrow(
                () -> new NotFoundException("Application with this id is not found.")
        );

        User applicationUser = foundApplication.getAssociatedUser();

        if(!applicationUser.equals(cancellingUser))
        {
            throw new ApiException(HttpStatus.FORBIDDEN, "This application does not belong to you," +
                    "so you cannot cancel it.");
        }
        chatApplicationRepository.delete(foundApplication);
    }

    public void cancelAllChatApplications(User user)
    {
        List<JoinChatApplication> foundApplications = chatApplicationRepository.findByAssociatedUser(user);

        chatApplicationRepository.deleteAll(foundApplications);
    }
}

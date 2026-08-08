package com.rebuild.backend.service.chat_services;

import com.rebuild.backend.model.dtos.user_dtos.ChatApplicationDisplayDTO;
import com.rebuild.backend.model.dtos.user_dtos.ChatApplicationFetchDTO;
import com.rebuild.backend.model.entities.chat_entities.*;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.entities.util_entitites.AbstractChat;
import com.rebuild.backend.model.enums.ChatStatus;
import com.rebuild.backend.model.responses.user_responses.ChatApplicationSearchResponse;
import com.rebuild.backend.repository.messaging_and_friendship_repositories.*;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import com.rebuild.backend.service.util_services.WebsocketsService;
import com.rebuild.backend.utils.exceptions.ApiException;
import com.rebuild.backend.utils.exceptions.ChatException;
import com.rebuild.backend.utils.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatAdministrationService {

    private final ChatParticipationRepository participationRepository;

    private final ChatRepository chatRepository;

    private final WebsocketsService websocketsService;

    private final UserRepository userRepository;

    private final ChatInvitationRepository chatInvitationRepository;

    private final MessageRepository messageRepository;

    private final ChatApplicationRepository chatApplicationRepository;

    private final ChatUtilService chatUtilService;

    @Autowired
    public ChatAdministrationService(ChatParticipationRepository participationRepository,
                                     ChatRepository chatRepository, WebsocketsService websocketsService,
                                     UserRepository userRepository, ChatInvitationRepository chatInvitationRepository, MessageRepository messageRepository, ChatApplicationRepository chatApplicationRepository, ChatUtilService chatUtilService) {
        this.participationRepository = participationRepository;
        this.chatRepository = chatRepository;
        this.websocketsService = websocketsService;
        this.userRepository = userRepository;
        this.chatInvitationRepository = chatInvitationRepository;
        this.messageRepository = messageRepository;
        this.chatApplicationRepository = chatApplicationRepository;
        this.chatUtilService = chatUtilService;
    }

    public boolean toggleUserAdmin(User administratingUser, UUID chatId, UUID userId)
    {
        GroupChat associatedChat = findParticipationAndCheckGroupAdminStatus(administratingUser, chatId);

        ChatParticipation recipientParticipation = participationRepository.
                findByParticipatingUser_IdAndParticipatedChat_Id(userId, chatId).orElseThrow(()
                        -> new NotFoundException("User with this ID is not found, or is not a member of this chat"));

        boolean currentStatus = recipientParticipation.getIsAdmin();

        //If the user is currently an admin, we will subtract one from the admin total,
        // since we are removing the admin status of the target user
        if (currentStatus)
        {
            associatedChat.setAdministratorCount(associatedChat.getAdministratorCount() - 1);
        }
        else
        {
            associatedChat.setAdministratorCount(associatedChat.getAdministratorCount() + 1);
        }
        chatRepository.save(associatedChat);

        recipientParticipation.setIsAdmin(!currentStatus);

        ChatParticipation savedParticipation = participationRepository.save(recipientParticipation);

        return savedParticipation.getIsAdmin();

    }

    public void kickUserFromChat(User kickingUser, UUID chatId, UUID userId)
    {
        GroupChat associatedChat = findParticipationAndCheckGroupAdminStatus(kickingUser, chatId);
        ChatParticipation kickedUserParticipation =
                participationRepository.findByParticipatingUser_IdAndParticipatedChat_Id(userId, chatId)
                        .orElseThrow(() -> new NotFoundException("This user is not a member of this chat"));


        chatUtilService.removeUserFromChat(associatedChat, kickedUserParticipation);
        if (kickedUserParticipation.getIsAdmin())
        {
            associatedChat.setAdministratorCount(associatedChat.getAdministratorCount() - 1);
        }

        GroupChat savedChat = chatRepository.save(associatedChat);
        websocketsService.sendKickNotification(savedChat, kickedUserParticipation);
    }

    public ChatInvitation sendGroupChatInvitation(User sender, UUID recipientId, UUID chatId)
    {
        User recipient = userRepository.findById(recipientId).orElseThrow(
                () -> new NotFoundException("User with the specified id not found"));

        GroupChat associatedChat = findParticipationAndCheckGroupAdminStatus(sender, chatId);

        Optional<ChatInvitation> foundInvitation =
                chatInvitationRepository.findBySenderAndRecipientAndAssociatedChat_Id(sender, recipient,
                        chatId);

        if (foundInvitation.isPresent()) {
            throw new ChatException(HttpStatus.CONFLICT,
                    "You already have an existing group chat invitation with this user, you cannot send " +
                            "another one.");
        }


        //This cast is safe, since we already know that it isn't a private chat by this point.
        ChatInvitation newInvitation = new ChatInvitation(sender, recipient,
                associatedChat);
        associatedChat.getInvitations().add(newInvitation);
        chatRepository.save(associatedChat);

        ChatInvitation savedInvitation = chatInvitationRepository.save(newInvitation);

        websocketsService.sendChatInvitationNotification(savedInvitation);
        return savedInvitation;
    }

    public boolean pinOrUnpinMessage(User pinningUser, UUID chatId, UUID messageId)
    {
        GroupChat _ = findParticipationAndCheckGroupAdminStatus(pinningUser, chatId);

        Message foundMessage = messageRepository.findByIdAndAssociatedChat_Id(messageId, chatId).orElseThrow(
                () -> new ChatException(HttpStatus.NOT_FOUND,
                        "The message with this id does not exist, or is not in this chat")
        );

        if(foundMessage.isRemoved())
        {
            throw new ChatException(HttpStatus.FORBIDDEN, "Removed messages cannot be pinned");
        }

        boolean oldStatus = foundMessage.isPinned();;
        foundMessage.setPinned(!oldStatus);
        Message savedMessage = messageRepository.save(foundMessage);
        return savedMessage.isPinned();

    }

    public String updateChatStatus(User updatingUser, UUID chatId, String newChatStatus)
    {
        GroupChat associatedChat = findParticipationAndCheckGroupAdminStatus(updatingUser, chatId);

        ChatStatus newStatus = ChatStatus.fromValue(newChatStatus);
        if (newStatus == null)
        {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid status value.");
        }
        associatedChat.setChatStatus(newStatus);

        chatRepository.save(associatedChat);

        return newStatus.value;

    }

    public ChatApplicationSearchResponse seeChatApplications(User user, UUID chatId,
                                                             int pageNumber)
    {
        if (pageNumber < 0)
        {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Page number must be greater than or equal to zero");
        }
        GroupChat associatedChat = findParticipationAndCheckGroupAdminStatus(user, chatId);


        PageRequest request = PageRequest.of(pageNumber, 10,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Slice<ChatApplicationFetchDTO> foundResults = chatApplicationRepository.findByAssociatedChat(associatedChat,
                request);

        List<ChatApplicationDisplayDTO> displayDTOS = foundResults.stream().map(ChatApplicationFetchDTO::toDisplayDTO)
                .toList();

        return new ChatApplicationSearchResponse(displayDTOS, foundResults.getNumber(), foundResults.hasNext());
    }

    public void acceptChatApplication(User user, UUID chatId, UUID applicationId)
    {
        GroupChat associatedChat = findParticipationAndCheckGroupAdminStatus(user, chatId);

        JoinChatApplication foundApplication = chatApplicationRepository.findByIdAndAssociatedChat(applicationId,
                        associatedChat).orElseThrow(() ->
                new NotFoundException("Application with this id not found or does not belong to this chat"));

        User applyingUser = foundApplication.getAssociatedUser();

        ChatParticipation newParticipation = chatUtilService.addUserToChat(associatedChat, applyingUser);

        participationRepository.save(newParticipation);

        chatRepository.save(associatedChat);

    }

    public void acceptAllApplications(User user, UUID chatId)
    {
        GroupChat associatedChat = findParticipationAndCheckGroupAdminStatus(user, chatId);

        List<JoinChatApplication> foundApplications = chatApplicationRepository.
                findByAssociatedChat(associatedChat);
        List<ChatParticipation> newParticipations = foundApplications.stream().map(application -> {
            User applyingUser =  application.getAssociatedUser();
            return chatUtilService.addUserToChat(associatedChat, applyingUser);
        }).collect(Collectors.toCollection(ArrayList::new));

        participationRepository.saveAll(newParticipations);
        chatRepository.save(associatedChat);
    }

    public void rejectChatApplication(User user, UUID chatId, UUID applicationId)
    {
        GroupChat associatedChat = findParticipationAndCheckGroupAdminStatus(user, chatId);

        JoinChatApplication foundApplication = chatApplicationRepository.findByIdAndAssociatedChat(applicationId,
                associatedChat).orElseThrow(() ->
                new NotFoundException("Application with this id not found or does not belong to this chat"));

        chatApplicationRepository.delete(foundApplication);
    }

    public void rejectAllApplications(User user, UUID chatId)
    {
        GroupChat associatedChat = findParticipationAndCheckGroupAdminStatus(user, chatId);

        List<JoinChatApplication> foundApplications = chatApplicationRepository.
                findByAssociatedChat(associatedChat);

        chatApplicationRepository.deleteAll(foundApplications);
    }

    private GroupChat findParticipationAndCheckGroupAdminStatus(User user, UUID chatId)
    {
        ChatParticipation foundParticipation = participationRepository.findByChatIdAndUser(
                chatId, user
        ).orElseThrow(() -> new ChatException(HttpStatus.NOT_FOUND, "The chat with this id either does not exist," +
                "or you are not a member in this chat."));
        AbstractChat associatedChat = foundParticipation.getParticipatedChat();

        if (!(associatedChat instanceof GroupChat))
        {
            throw new ChatException(HttpStatus.FORBIDDEN, "This action can only be done on group chats");
        }
        if (!foundParticipation.getIsAdmin()) {
            throw new ChatException(HttpStatus.FORBIDDEN, "Only administrators are able to do this operation");
        }

        return (GroupChat) associatedChat;
    }
}

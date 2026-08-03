package com.rebuild.backend.service.chat_services;

import com.rebuild.backend.model.entities.messaging_and_friendship_entities.ChatInvitation;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.ChatParticipation;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.GroupChat;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.Message;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.entities.util_entitites.AbstractChat;
import com.rebuild.backend.repository.messaging_and_friendship_repositories.ChatInvitationRepository;
import com.rebuild.backend.repository.messaging_and_friendship_repositories.ChatParticipationRepository;
import com.rebuild.backend.repository.messaging_and_friendship_repositories.ChatRepository;
import com.rebuild.backend.repository.messaging_and_friendship_repositories.MessageRepository;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import com.rebuild.backend.service.util_services.WebsocketsService;
import com.rebuild.backend.utils.exceptions.ChatException;
import com.rebuild.backend.utils.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ChatAdministrationService {

    private final ChatParticipationRepository participationRepository;

    private final ChatRepository chatRepository;

    private final WebsocketsService websocketsService;

    private final UserRepository userRepository;

    private final ChatInvitationRepository chatInvitationRepository;

    private final MessageRepository messageRepository;

    @Autowired
    public ChatAdministrationService(ChatParticipationRepository participationRepository,
                                     ChatRepository chatRepository, WebsocketsService websocketsService,
                                     UserRepository userRepository, ChatInvitationRepository chatInvitationRepository, MessageRepository messageRepository) {
        this.participationRepository = participationRepository;
        this.chatRepository = chatRepository;
        this.websocketsService = websocketsService;
        this.userRepository = userRepository;
        this.chatInvitationRepository = chatInvitationRepository;
        this.messageRepository = messageRepository;
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

        associatedChat.getParticipations().remove(kickedUserParticipation);

        associatedChat.setMemberCount(associatedChat.getMemberCount() - 1);
        if (kickedUserParticipation.getIsAdmin())
        {
            associatedChat.setAdministratorCount(associatedChat.getAdministratorCount() - 1);
        }

        GroupChat savedChat = chatRepository.save(associatedChat);
        participationRepository.delete(kickedUserParticipation);
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
        ChatParticipation pinningUserParticipation = participationRepository.findByChatIdAndUser(chatId, pinningUser).
                orElseThrow(() -> new ChatException(HttpStatus.NOT_FOUND, "The chat with this id either does not exist," +
                        "or you are not a member in this chat."));

        if (!pinningUserParticipation.getIsAdmin()){
            throw new ChatException(HttpStatus.FORBIDDEN, "Only administrators can perform this action");
        }

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

    private GroupChat findParticipationAndCheckGroupAdminStatus(User user, UUID chatId)
    {
        ChatParticipation foundParticipation = participationRepository.findByChatIdAndUser(
                chatId, user
        ).orElseThrow(() -> new ChatException(HttpStatus.NOT_FOUND, "The chat with this id either does not exist," +
                "or you are not a member in this chat, or this chat is not a group chat"));
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

package com.rebuild.backend.service.forum_services;

import com.rebuild.backend.model.entities.messaging_and_friendship_entities.ChatInvitation;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.ChatParticipation;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.GroupChat;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.entities.util_entitites.base_entities.AbstractChat;
import com.rebuild.backend.repository.messaging_and_friendship_repositories.ChatInvitationRepository;
import com.rebuild.backend.repository.messaging_and_friendship_repositories.ChatParticipationRepository;
import com.rebuild.backend.repository.messaging_and_friendship_repositories.ChatRepository;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import com.rebuild.backend.utils.exceptions.ChatException;
import com.rebuild.backend.utils.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class ChatAdministrationService {

    private final ChatParticipationRepository participationRepository;

    private final ChatRepository chatRepository;

    private final WebsocketsService websocketsService;

    private final UserRepository userRepository;

    private final ChatInvitationRepository chatInvitationRepository;

    @Autowired
    public ChatAdministrationService(ChatParticipationRepository participationRepository,
                                     ChatRepository chatRepository, WebsocketsService websocketsService,
                                     UserRepository userRepository, ChatInvitationRepository chatInvitationRepository) {
        this.participationRepository = participationRepository;
        this.chatRepository = chatRepository;
        this.websocketsService = websocketsService;
        this.userRepository = userRepository;
        this.chatInvitationRepository = chatInvitationRepository;
    }

    @Transactional
    public boolean toggleUserAdmin(User administratingUser, UUID chatId, UUID userId)
    {
        ChatParticipation foundParticipation = participationRepository.findByChatIdAndUserAndGroupChatStatus(
                chatId, administratingUser, true
        ).orElseThrow(() -> new ChatException(HttpStatus.NOT_FOUND, "The chat with this id either does not exist," +
                "or you are not a member in this chat, or this chat is not a group chat"));

        if (foundParticipation.hasNoAdminPrivileges())
        {
            throw new ChatException(HttpStatus.UNAUTHORIZED, "Only administrators are able to do this operation");
        }

        ChatParticipation recipientParticipation = participationRepository.
                findByParticipatingUser_IdAndParticipatedChat_Id(userId, chatId).orElseThrow(()
                        -> new NotFoundException("User with this ID is not found, or is not a member of this chat"));

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

        ChatParticipation kickingUserParticipation = participationRepository.findByChatIdAndUserAndGroupChatStatus(
                chatId, kickingUser, true
        ).orElseThrow(() -> new ChatException(HttpStatus.NOT_FOUND, "The chat with this id either does not exist," +
                "or you are not a member in this chat, or this chat is not a group chat"));

        if (kickingUserParticipation.hasNoAdminPrivileges())
        {
            throw new ChatException(HttpStatus.UNAUTHORIZED, "Only administrators are able to do this operation");
        }

        ChatParticipation kickedUserParticipation =
                participationRepository.findByParticipatingUser_IdAndParticipatedChat_Id(userId, chatId)
                        .orElseThrow(() -> new NotFoundException("This user is not a member of this chat"));

        if (kickedUserParticipation.getIsGroupOwner()) {
            throw new ChatException(HttpStatus.FORBIDDEN, "The group owner cannot be kicked");
        }

        AbstractChat necessaryChat = kickedUserParticipation.getParticipatedChat();
        necessaryChat.getParticipations().remove(kickedUserParticipation);

        AbstractChat savedChat = chatRepository.save(necessaryChat);

        //Once again, this is a safe cast, since we know that the chat cannot be a private chat by the time we get here.
        websocketsService.sendKickNotification((GroupChat) savedChat, kickedUserParticipation);
    }

    @Transactional
    public void deleteChat(User deletingUser, UUID chatId)
    {

        ChatParticipation deletingUserParticipation = participationRepository.findByChatIdAndUserAndGroupChatStatus(
                chatId, deletingUser, true
        ).orElseThrow(() -> new ChatException(HttpStatus.NOT_FOUND, "The chat with this id either does not exist," +
                "or you are not a member in this chat, or this chat is not a group chat"));

        if (!deletingUserParticipation.getIsGroupOwner())
        {
            throw new ChatException(HttpStatus.FORBIDDEN, "Only the group owner can disband the group");
        }

        AbstractChat relevantChat = deletingUserParticipation.getParticipatedChat();
        //This will automatically delete all the messages and participations as well, because of orphan removal
        relevantChat.setMessages(null);
        relevantChat.setParticipations(null);
        chatRepository.delete(relevantChat);
    }


    @Transactional
    public void transferChatOwnership(User transferingUser, UUID chatId, UUID userId) {
        ChatParticipation transferringUserParticipation = participationRepository.findByChatIdAndUserAndGroupChatStatus(
                chatId, transferingUser, true
        ).orElseThrow(() -> new ChatException(HttpStatus.NOT_FOUND, "The chat with this id either does not exist," +
                "or you are not a member in this chat, or this chat is not a group chat"));

        if (!transferringUserParticipation.getIsGroupOwner())
        {
            throw new ChatException(HttpStatus.UNAUTHORIZED, "You are not the owner of this chat");
        }

        ChatParticipation recipientParticipation = participationRepository.
                findByParticipatingUser_IdAndParticipatedChat_Id(userId, chatId).orElseThrow(()
                        -> new NotFoundException("The recipient is not a member of this chat"));


        // The user becomes the owner, and thus an admin, and the user that transfers ownership loses it
        // but still retains being an admin
        recipientParticipation.setIsGroupOwner(true);
        recipientParticipation.setIsAdmin(true);
        transferringUserParticipation.setIsGroupOwner(false);

        participationRepository.save(transferringUserParticipation);
        participationRepository.save(recipientParticipation);
    }


    @Transactional
    public ChatInvitation sendGroupChatInvitation(User sender, UUID recipientId, UUID chatId)
    {
        User recipient = userRepository.findById(recipientId).orElseThrow(
                () -> new NotFoundException("User with the specified id not found"));

        ChatParticipation foundParticipation = participationRepository.findByChatIdAndUserAndGroupChatStatus(
                chatId, sender, true
        ).orElseThrow(() -> new ChatException(HttpStatus.NOT_FOUND, "The chat with this id either does not exist," +
                "or you are not a member in this chat, or this chat is not a group chat"));

        if (foundParticipation.hasNoAdminPrivileges())
        {
            throw new ChatException(HttpStatus.UNAUTHORIZED, "You can't invite anyone to this chat " +
                    "because you are not an administrator in this chat.");
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
        ChatInvitation newInvitation = new ChatInvitation(sender, recipient,
                (GroupChat) foundParticipation.getParticipatedChat());

        ChatInvitation savedInvitation = chatInvitationRepository.save(newInvitation);

        websocketsService.sendChatInvitationNotification(savedInvitation);
        return savedInvitation;
    }
}

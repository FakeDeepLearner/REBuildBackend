package com.rebuild.backend.service.chat_services;

import com.rebuild.backend.model.dtos.forum_dtos.message_and_chat_dtos.MessageDisplayDTO;
import com.rebuild.backend.model.dtos.forum_dtos.message_and_chat_dtos.MessageSearchDTO;
import com.rebuild.backend.model.dtos.forum_dtos.message_and_chat_dtos.PinnedMessageDTO;
import com.rebuild.backend.model.entities.chat_entities.GroupChat;
import com.rebuild.backend.model.entities.chat_entities.Message;
import com.rebuild.backend.model.entities.chat_entities.PrivateChat;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.entities.util_entitites.AbstractChat;
import com.rebuild.backend.model.responses.forum_responses.*;
import com.rebuild.backend.repository.chat_repositories.ChatParticipationRepository;
import com.rebuild.backend.repository.chat_repositories.GroupChatRepository;
import com.rebuild.backend.repository.chat_repositories.MessageRepository;
import com.rebuild.backend.repository.chat_repositories.PrivateChatRepository;
import com.rebuild.backend.service.util_services.WebsocketsService;
import com.rebuild.backend.utils.UserPair;
import com.rebuild.backend.utils.exceptions.ApiException;
import com.rebuild.backend.utils.exceptions.BelongingException;
import com.rebuild.backend.utils.exceptions.ChatException;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import com.rebuild.backend.utils.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class MessageService {

    private final WebsocketsService websocketsService;

    private final UserRepository userRepository;

    private final ChatParticipationRepository participationRepository;

    private final MessageRepository messageRepository;
    
    private final PrivateChatRepository privateChatRepository;
    
    private final GroupChatRepository groupChatRepository;
    
    private final ChatUtilService chatUtilService;

    @Autowired
    public MessageService(WebsocketsService websocketsService, UserRepository userRepository,
                          ChatParticipationRepository participationRepository, MessageRepository messageRepository,
                          PrivateChatRepository privateChatRepository, GroupChatRepository groupChatRepository, ChatUtilService chatUtilService) {
        this.websocketsService = websocketsService;
        this.userRepository = userRepository;
        this.participationRepository = participationRepository;
        this.messageRepository = messageRepository;
        this.privateChatRepository = privateChatRepository;
        this.groupChatRepository = groupChatRepository;
        this.chatUtilService = chatUtilService;
    }

    private MessageDisplayDTO sendMessage(User sender, AbstractChat chat, String content)
    {
        Message newMessage = chatUtilService.createNewMessageInChat(chat, sender, content, true);

        return newMessage.toDTo(sender);
    }

    public MessageDisplayDTO sendMessageInPrivateChat(User sender, UUID chatId, String content)
    {
        if (content.isBlank())
        {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Message content cannot be blank");
        }
        
        PrivateChat chat = privateChatRepository.findById(chatId).orElseThrow(
                () -> new NotFoundException("Private chat with this id does not exist.")
        );
        
        if (!participationRepository.existsByAssociatedChatAndParticipatingUser(chat, sender))
        {
            throw new ApiException(HttpStatus.FORBIDDEN, "You are not allowed to send this message," +
                    "because you are not a member of this chat");
        }

        return sendMessage(sender, chat, content);
    }

    public MessageDisplayDTO sendMessageInGroupChat(User sender, UUID chatId, String content)
    {
        if (content.isBlank())
        {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Message content cannot be blank");
        }

        GroupChat chat = groupChatRepository.findById(chatId).orElseThrow(
                () -> new NotFoundException("Group chat with this id does not exist.")
        );

        if (!participationRepository.existsByAssociatedChatAndParticipatingUser(chat, sender))
        {
            throw new ApiException(HttpStatus.FORBIDDEN, "You are not allowed to send this message," +
                    "because you are not a member of this chat");
        }

        return sendMessage(sender, chat, content);
    }
    
    public MessageDisplayDTO sendMessageToUser(User sender, UUID userId, String content)
    {
        if (content.isBlank())
        {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Message content cannot be blank");
        }
        if(userId.equals(sender.getId()))
        {
            throw new ApiException(HttpStatus.FORBIDDEN, "You cannot send messages to yourself.");
        }
        
        User recipient = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User with this id does not exist.")
        );
        
        UserPair pair = new UserPair(sender, recipient);
        Optional<PrivateChat> foundChat = privateChatRepository.findByLowUserIdAndHighUserId(
                pair.lowId(),
                pair.highId()
        );
        //If the 2 users already have a chat between them, then we use the existing method to send the message.
        if (foundChat.isPresent())
        {
            return sendMessage(sender, foundChat.get(), content);
        }

        // If not, then we create a new private chat and then make a new message in that chat.
        // This requires a different websocket notification, so it does not
        // use the same methods as the other send message methods.
        PrivateChat createdChat = new PrivateChat(sender, recipient, content);

        Message newMessage = chatUtilService.createNewMessageInChat(createdChat, sender, content,
                false);

        privateChatRepository.save(createdChat);

        websocketsService.sendNewChatNotification(createdChat, sender, newMessage, recipient);

        return newMessage.toDTo(sender);
    }


    public MessageDisplayDTO removeMessage(User removingUser, UUID messageId)
    {
        Message foundMessage = messageRepository.findByIdAndSender(messageId, removingUser).
                orElseThrow(() -> new BelongingException("Message with this id either does not exist or " +
                        "does not belong to you"));

        //Removing a message also unpins it if it is pinned.
        foundMessage.setRemoved(true);
        foundMessage.setPinned(false);

        Message savedMessage = messageRepository.save(foundMessage);

        // The message should always be displayed on the right,
        // because the user can only remove their own messages anyway
        return savedMessage.toDTo(removingUser);
    }

    public MessageDisplayDTO editMessage(User editingUser, UUID messageId, String newContent)
    {
        if (newContent.isBlank())
        {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Message content cannot be blank");
        }
        Message foundMessage = messageRepository.findByIdAndSender(messageId, editingUser).orElseThrow(
                () -> new BelongingException("Message with this id does not exist or " +
                        "does not belong to you")
        );

        foundMessage.setContent(newContent);
        foundMessage.setEdited(true);

        Message savedMessage = messageRepository.save(foundMessage);

        return savedMessage.toDTo(editingUser);
    }

    public SearchMessagesResponse searchForMessages(User searchingUser, UUID chatId, String query,
                                                    int pageNumber)
    {
        if (query.isBlank())
        {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Search query cannot be blank");
        }

        if (pageNumber < 0)
        {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Page number must be greater than or equal to 0.");
        }

        boolean userIsInChat =
                participationRepository.existsByAssociatedChat_IdAndParticipatingUser(chatId, searchingUser);

        if (!userIsInChat)
        {
            throw new BelongingException("You are not a member of this chat");
        }

        Pageable pageable = PageRequest.of(pageNumber, 25,
                Sort.by(Sort.Direction.ASC, "createdAt"));

        Slice<Message> foundMessages = messageRepository.findByChatAndSimilarContent(chatId, query, pageable);

        List<MessageSearchDTO> displayedSearchDTOs = foundMessages.stream().map(Message::toSearchDTO)
                .toList();

        return new SearchMessagesResponse(displayedSearchDTOs, foundMessages.hasNext());
    }

    public LoadMoreMessagesResponse loadMoreMessages(User searchingUser, UUID chatId,
                                                     int pageNumber)
    {
        if (pageNumber < 0)
        {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Page number must be greater than or equal to 0.");
        }

        boolean userIsInChat =
                participationRepository.existsByAssociatedChat_IdAndParticipatingUser(chatId, searchingUser);

        if (!userIsInChat)
        {
            throw new BelongingException("You are not a member of this chat");
        }

        Pageable pageable = PageRequest.of(pageNumber, 25,
                Sort.by(Sort.Direction.ASC, "createdAt"));

        Slice<Message> currentMessages = messageRepository.findByAssociatedChat_Id(chatId, pageable);

        List<MessageDisplayDTO> displayedMessages =
            currentMessages.stream().map(message -> message.toDTo(searchingUser)).toList();

        return new LoadMoreMessagesResponse(displayedMessages, currentMessages.hasNext());
    }

    public PinnedMessagesResponse getPinnedMessages(User user, UUID chatId, int pageNumber)
    {
        if (pageNumber < 0)
        {
            throw new ChatException(HttpStatus.BAD_REQUEST, "Page number cannot be negative");
        }
        boolean userIsInChat = participationRepository.
                existsByAssociatedChat_IdAndParticipatingUser(chatId, user);

        if (!userIsInChat)
        {
            throw new BelongingException("You are not a member of this chat, or a chat with this ID does not exist");
        }

        Pageable pageable = PageRequest.of(pageNumber, 20,  Sort.by(Sort.Direction.ASC, "createdAt"));
        Slice<Message> allPinnedMessages = messageRepository.findPinnedMessagesByChatId(chatId, pageable);

        List<PinnedMessageDTO> pinnedMessageDTOS = allPinnedMessages.stream().map(
                Message::toPinnedDTO
        ).toList();

        return new PinnedMessagesResponse(pinnedMessageDTOS, allPinnedMessages.hasNext());
    }
}

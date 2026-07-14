package com.rebuild.backend.service.forum_services;

import com.rebuild.backend.model.dtos.forum_dtos.message_and_chat_dtos.FetchMessagesDTO;
import com.rebuild.backend.model.dtos.forum_dtos.message_and_chat_dtos.MessageDisplayDTO;
import com.rebuild.backend.model.dtos.forum_dtos.message_and_chat_dtos.MessageSearchDTO;
import com.rebuild.backend.model.dtos.forum_dtos.message_and_chat_dtos.PinnedMessageDTO;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.*;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.entities.util_entitites.base_entities.AbstractChat;
import com.rebuild.backend.model.responses.forum_responses.*;
import com.rebuild.backend.repository.messaging_and_friendship_repositories.*;
import com.rebuild.backend.utils.exceptions.ApiException;
import com.rebuild.backend.utils.exceptions.BelongingException;
import com.rebuild.backend.utils.exceptions.ChatException;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class MessageService {

    private final WebsocketsService websocketsService;

    private final ChatRepository chatRepository;

    private final UserRepository userRepository;

    private final FriendRelationshipRepository friendRelationshipRepository;

    private final ChatParticipationRepository participationRepository;

    private final MessageRepository messageRepository;

    @Autowired
    public MessageService(WebsocketsService websocketsService, ChatRepository chatRepository, UserRepository userRepository,
                          FriendRelationshipRepository friendRelationshipRepository,
                          ChatParticipationRepository participationRepository,
                          MessageRepository messageRepository) {
        this.websocketsService = websocketsService;
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.friendRelationshipRepository = friendRelationshipRepository;
        this.participationRepository = participationRepository;
        this.messageRepository = messageRepository;
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

        return savedMessage.toDTo(sender);
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

        return savedMessage.toDTo(sender);
    }


    
    public MessageDisplayDTO createMessage(User sender, UUID receivingObjectId, String messageContent)
    {
        if (messageContent.isBlank())
        {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Message content cannot be blank");
        }
        Optional<User> recipient = userRepository.findById(receivingObjectId);

        //If we find a user with the given id, the recipient will receive a message from this user for the first time
        //So, create a private chat between the 2 users and send the message
        if (recipient.isPresent())
        {
           User receivingUser = recipient.get();

            // If the recipient has not selected the setting, just send the message with the content,
            // creating a chat between the users first
            if(!receivingUser.isMessagesFromFriendsOnly())
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

    private List<Message> getMaximumSize(List<Message> original, int maxSize)
    {
        if (maxSize >= original.size())
        {
            return original;
        }
        return original.subList(0, maxSize);
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
                participationRepository.existsByParticipatedChat_IdAndParticipatingUser(chatId, searchingUser);

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
    
    public LoadMoreMessagesResponse loadMoreMessagesWithTimestamp(User searchingUser, UUID chatId,
                                                                  String lastTimestamp, boolean loadFromAbove)
    {
        try {
            Instant timestamp = Instant.parse(lastTimestamp);
            if (loadFromAbove) {
                return loadMoreFromAbove(searchingUser, chatId, timestamp);
            }

            return loadMoreFromBelow(searchingUser, chatId, timestamp);
        }
        catch (DateTimeParseException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid timestamp format");
        }
    }

    private FetchMessagesDTO fetchMessagesBeforeOrAfterTimestamp(UUID chatId, Instant lastTimestamp,
                                                                   boolean fetchMessagesBefore)
    {
        List<Message> messages;

        //This is how many messages we will fetch in each direction. We will actually limit ourselves to 1 more
        // so that we can easily check if we have more data in either direction
        int fetchSize = 25;

        Limit messageFetchLimit = Limit.of(fetchSize + 1);

        if (fetchMessagesBefore) {
            messages = messageRepository.findByAssociatedChat_IdAndCreatedAtBefore(
                    chatId, lastTimestamp,
                    Sort.by(Sort.Direction.ASC, "createdAt"),
                    messageFetchLimit
            );
        }
        else {
            messages = messageRepository.findByAssociatedChat_IdAndCreatedAtAfter(
                    chatId, lastTimestamp,
                    Sort.by(Sort.Direction.ASC, "createdAt"),
                    messageFetchLimit
            );
        }

        boolean hasMore = messages.size() > fetchSize;
        return new FetchMessagesDTO(getMaximumSize(messages, fetchSize), hasMore);
    }
    
    private LoadMoreMessagesResponse loadMoreFromAbove(User searchingUser, UUID chatId,
                                                         Instant lastTimestamp)
    {
        boolean userIsInChat = participationRepository.
                existsByParticipatedChat_IdAndParticipatingUser(chatId, searchingUser);

        if (!userIsInChat)
        {
            throw new BelongingException("You are not a member of this chat, or a chat with this ID does not exist");
        }

        FetchMessagesDTO fetchMessagesDTO = fetchMessagesBeforeOrAfterTimestamp(chatId, lastTimestamp, true);

        List<Message> messages = fetchMessagesDTO.messages();

        Instant lastBeforeTimestamp = messages.getFirst().getCreatedAt();


        List<MessageDisplayDTO> displayedMessages =
                messages.stream().map(message -> message.toDTo(searchingUser)).toList();

        return new LoadMoreMessagesResponse(displayedMessages, fetchMessagesDTO.hasMore(), lastBeforeTimestamp);
    }


    private LoadMoreMessagesResponse loadMoreFromBelow(User searchingUser, UUID chatId,
                                                         Instant lastTimestamp)
    {

        boolean userIsInChat = participationRepository.
                existsByParticipatedChat_IdAndParticipatingUser(chatId, searchingUser);

        if (!userIsInChat)
        {
            throw new BelongingException("You are not a member of this chat, or a chat with this ID does not exist");
        }

        FetchMessagesDTO fetchMessagesDTO = fetchMessagesBeforeOrAfterTimestamp(chatId, lastTimestamp,
                false);

        List<Message> messages = fetchMessagesDTO.messages();

        Instant lastAfterTimestamp = messages.getLast().getCreatedAt();


        List<MessageDisplayDTO> displayedMessages =
                messages.stream().map(message -> message.toDTo(searchingUser)).toList();

        return new LoadMoreMessagesResponse(displayedMessages, fetchMessagesDTO.hasMore(), lastAfterTimestamp);
    }

    public PinnedMessagesResponse getPinnedMessages(User user, UUID chatId, int pageNumber)
    {
        if (pageNumber < 0)
        {
            throw new ChatException(HttpStatus.BAD_REQUEST, "Page number cannot be negative");
        }
        boolean userIsInChat = participationRepository.
                existsByParticipatedChat_IdAndParticipatingUser(chatId, user);

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

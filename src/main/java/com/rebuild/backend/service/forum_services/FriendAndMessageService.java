package com.rebuild.backend.service.forum_services;

import com.rebuild.backend.model.entities.messaging_and_friendship_entities.*;
import com.rebuild.backend.model.entities.profile_entities.ProfilePicture;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.exceptions.BelongingException;
import com.rebuild.backend.model.forms.dtos.StatusAndError;
import com.rebuild.backend.model.forms.dtos.forum_dtos.FriendRequestDTO;
import com.rebuild.backend.model.forms.dtos.forum_dtos.MessageDisplayDTO;
import com.rebuild.backend.model.forms.dtos.forum_dtos.UsernameSearchResultDTO;
import com.rebuild.backend.model.responses.DisplayChatResponse;
import com.rebuild.backend.model.responses.LoadChatResponse;
import com.rebuild.backend.repository.forum_repositories.ChatRepository;
import com.rebuild.backend.repository.forum_repositories.FriendRelationshipRepository;
import com.rebuild.backend.repository.forum_repositories.FriendRequestRepository;
import com.rebuild.backend.repository.forum_repositories.MessageRepository;
import com.rebuild.backend.repository.user_repositories.ProfilePictureRepository;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import com.rebuild.backend.service.util_services.RabbitProducingService;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.job.parameters.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FriendAndMessageService {

    private final JobOperator jobOperator;

    private final UserRepository userRepository;
    private final FriendRelationshipRepository friendRelationshipRepository;

    private final ChatRepository chatRepository;

    private final MessageRepository messageRepository;

    private final ProfilePictureRepository profilePictureRepository;

    private final FriendRequestRepository friendRequestRepository;

    private final RabbitProducingService rabbitProducingService;

    @Autowired
    public FriendAndMessageService(JobOperator jobOperator, UserRepository userRepository,
                                   FriendRelationshipRepository friendRelationshipRepository,
                                   ChatRepository chatRepository, MessageRepository messageRepository, ProfilePictureRepository profilePictureRepository, FriendRequestRepository friendRequestRepository, RabbitProducingService rabbitProducingService) {
        this.jobOperator = jobOperator;
        this.userRepository = userRepository;
        this.friendRelationshipRepository = friendRelationshipRepository;
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
        this.profilePictureRepository = profilePictureRepository;
        this.friendRequestRepository = friendRequestRepository;
        this.rabbitProducingService = rabbitProducingService;
    }

    @Transactional
    public StatusAndError acceptFriendshipRequest(User receiver, UUID friendRequestId)
    {
        FriendRequest friendRequest = friendRequestRepository.findByIdAndRecipient(friendRequestId, receiver).
                orElseThrow(() ->
                        new BelongingException("This friend request either does not exist or has not been addressed to you."));


        User sender = friendRequest.getSender();

        FriendRelationship newRelationship = new FriendRelationship(sender, receiver);

        friendRelationshipRepository.save(newRelationship);

        friendRequestRepository.delete(friendRequest);

        return new StatusAndError(HttpStatus.OK, "You have added " + sender.getUsername() + " as a friend");

    }

    @Transactional
    public StatusAndError declineFriendshipRequest(User declininguser, UUID friendRequestId)
    {
        FriendRequest friendRequest = friendRequestRepository.findByIdAndRecipient(friendRequestId, declininguser).
                orElseThrow(() ->
                        new BelongingException("This friend either does not exist or has not been addressed to you."));

        friendRequestRepository.delete(friendRequest);

        return new StatusAndError(HttpStatus.OK, "Friend request declined");

    }


    @Transactional
    public StatusAndError sendFriendRequest(User sender, UUID recipientId)
    {
        User recipient = userRepository.findById(recipientId).orElse(null);

        assert recipient != null : "Recipient not found";

        Optional<FriendRequest> foundRequest =
                friendRequestRepository.findByTwoUsers(sender, recipient);

        if (foundRequest.isPresent()) {
            return new StatusAndError(HttpStatus.CONFLICT,
                    "You already have an existing friend request with this user, you cannot send " +
                            "another one until it is either declined or times out.");
        }

        Optional<FriendRelationship> foundRelationship =
                friendRelationshipRepository.findByTwoUsers(sender, recipient);
        if (foundRelationship.isPresent()) {
            return new StatusAndError(HttpStatus.CONFLICT,
                    "You are already friends with this user");
        }

        FriendRequestDTO friendRequestDTO = new FriendRequestDTO(sender, recipientId);

        rabbitProducingService.sendFriendshipRequest(friendRequestDTO);
        return new StatusAndError(HttpStatus.ACCEPTED, "The request has been sent");
    }

    private Chat createChatBetween(User sender, User recipient)
    {
        Chat newChat = new Chat(sender, recipient);
        sender.addSenderChat(newChat);
        recipient.addReceiverChat(newChat);
        return chatRepository.save(newChat);
    }

    private Message createNewMessage(User sender, User recipient, String messageContent){
        //If we enter this method, we know that we will have to create a new chat.
        Chat createdChat = createChatBetween(sender, recipient);

        Message newMessage = new Message(sender, recipient, messageContent);
        newMessage.setAssociatedChat(createdChat);
        createdChat.getMessages().add(newMessage);

        return messageRepository.save(newMessage);
    }

    private Message createNewMessage(User sender, User recipient, String content,
                                           Chat associatedChat)
    {

        Message newMessage = new Message(sender, recipient, content);
        newMessage.setAssociatedChat(associatedChat);
        associatedChat.getMessages().add(newMessage);

        return messageRepository.save(newMessage);
    }


    public Message createMessage(User sender, UUID recipientId, String messageContent)
    {
        User recipient = userRepository.findById(recipientId).orElse(null);
        assert recipient != null : "User with this ID not found";

        // If the 2 users have a chat together,
        // immediately send the message no matter the recipient's profile settings
        Optional<Chat> foundChat = chatRepository.findByTwoUsers(sender, recipient);
        if (foundChat.isPresent()) {
            return createNewMessage(sender, recipient, messageContent, foundChat.get());
        }

        //If the recipient has not selected the setting, just send the message with the content.
        if(!recipient.getProfile().isMessagesFromFriendsOnly())
        {
            return createNewMessage(sender, recipient, messageContent);
        }


        Optional<FriendRelationship> foundRelationship =
                friendRelationshipRepository.findByTwoUsers(sender, recipient);
        if (foundRelationship.isPresent()) {
            return createNewMessage(sender, recipient, messageContent);
        }
        return null;

    }

    public List<DisplayChatResponse> displayAllChats(User displayingUser)
    {
        List<Chat> allChats = chatRepository.findByUser(displayingUser);

        return allChats.stream()
                .map(chat -> {
                    User chatInitiator = chat.getInitiatingUser();
                    User chatReceiver = chat.getReceivingUser();


                    User otherChatUser = chatInitiator.equals(displayingUser) ? chatReceiver : chatInitiator;

                    String picture_url = profilePictureRepository.findByUserId(otherChatUser.getId()).
                            map(ProfilePicture::getSecure_url).orElse(null);
                    UUID chatId = chat.getId();
                    String username = otherChatUser.getUsername();

                    return new DisplayChatResponse(chatId, username, picture_url);
                }).toList();
    }

    public LoadChatResponse loadChat(UUID chatId, User loadingUser)
    {
        Chat chat = chatRepository.findById(chatId).orElse(null);
        assert chat != null : "Chat with this ID not found";
        User receiver = chat.getReceivingUser();
        UUID userID = receiver.getId();
        String userName = receiver.getForumUsername();
        Optional<ProfilePicture> foundPicture = profilePictureRepository.findByUserId(userID);

        List<MessageDisplayDTO> messages = chat.getMessages()
                .stream().
                map(message -> {
                    boolean displayOnTheRight = message.getSender().equals(loadingUser);
                    return new MessageDisplayDTO(message.getContent(), message.getCreatedAt(),
                            displayOnTheRight);
                }).toList();


        String pictureUrl = foundPicture.map(ProfilePicture::getSecure_url).orElse(null);
        return new LoadChatResponse(userName, userID, messages, pictureUrl);
    }

    public List<UsernameSearchResultDTO> loadUserInbox(User loadingUser)
    {
        return loadingUser.getInbox().getFriendRequests().stream()
                .map(request -> {
                    UUID userID = request.getSender().getId();
                    String userName = request.getSender().getForumUsername();
                    return new UsernameSearchResultDTO(userID, userName);
                }).toList();

    }

    /*
     * We use this method to create parameters for each job
     * separately, because we can't use the same timestamp value for the 3 different jobs we want to run.
     * */
    private JobParametersBuilder createParameters(Job runningJob)
    {
        return new JobParametersBuilder().
                addLong("timestamp", System.currentTimeMillis()).
                addString("name", runningJob.getName());
    }


    //Every 10 seconds
    @Scheduled(fixedRate = 10 * 1000)
    public void runLikesUpdatingJob(@Qualifier(value = "friendLikeJob") Job friendLikeJob)
            throws JobInstanceAlreadyCompleteException,
            JobExecutionAlreadyRunningException,
            JobParametersInvalidException, JobRestartException, NoSuchJobException {

        jobOperator.start(friendLikeJob, createParameters(friendLikeJob).toJobParameters());
    }

    @Scheduled(cron = "@midnight")
    public void runFriendRequestsJob(@Qualifier(value = "friendRequestJob") Job friendRequestJob)
            throws JobInstanceAlreadyCompleteException,
            JobExecutionAlreadyRunningException,
            JobParametersInvalidException, JobRestartException, NoSuchJobException
    {
        jobOperator.start(friendRequestJob, createParameters(friendRequestJob)
                .addString("dateCutoff", LocalDateTime.now().minusDays(7).toString()).toJobParameters());
    }


}

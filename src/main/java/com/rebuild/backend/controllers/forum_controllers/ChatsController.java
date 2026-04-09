package com.rebuild.backend.controllers.forum_controllers;

import com.rebuild.backend.model.entities.messaging_and_friendship_entities.AbstractChat;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.ChatInvitation;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.GroupChat;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.responses.DisplayChatResponse;
import com.rebuild.backend.model.responses.LoadChatResponse;
import com.rebuild.backend.service.forum_services.ChatAndMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/chats")
public class ChatsController {

    private final ChatAndMessageService chatAndMessageService;

    @Autowired
    public ChatsController(ChatAndMessageService chatAndMessageService) {
        this.chatAndMessageService = chatAndMessageService;
    }


    @GetMapping("/all_chats")
    public List<DisplayChatResponse> showAllChats(@AuthenticationPrincipal User authenticatedUser) {
        return chatAndMessageService.displayAllChats(authenticatedUser);
    }

    @GetMapping("/load/{chat_id}")
    @ResponseStatus(HttpStatus.OK)
    public LoadChatResponse loadChat(@PathVariable UUID chat_id,
                                     @AuthenticationPrincipal User authenticatedUser) {
        return chatAndMessageService.loadChat(chat_id, authenticatedUser);
    }

    @PostMapping("/send_message/{receiving_object_id}")
    @ResponseStatus(HttpStatus.OK)
    public AbstractChat sendMessage(@PathVariable UUID receiving_object_id,
                                         @RequestBody String messageContent,
                                         @AuthenticationPrincipal User authenticatedUser) {
        return chatAndMessageService.
                createMessage(authenticatedUser, receiving_object_id, messageContent);

    }

    @PostMapping("/create")
    public GroupChat createGroupChat(@AuthenticationPrincipal User creatingUser, @RequestBody String name)
    {
        return chatAndMessageService.createNewGroupChat(creatingUser, name);
    }


    @PostMapping("/reject_invite/{invitation_id}")
    public void rejectChatInvitation(@AuthenticationPrincipal User rejectingUser, @PathVariable UUID invitation_id)
    {
        chatAndMessageService.declineChatInvitation(rejectingUser, invitation_id);
    }

    @PostMapping("/accept_invite/{invitation_id}")
    @ResponseStatus(HttpStatus.OK)
    public GroupChat acceptChatInvitation(@AuthenticationPrincipal User acceptingUser,
                                                          @PathVariable UUID invitation_id) {
        return chatAndMessageService.acceptChatInvitation(acceptingUser, invitation_id);
    }


    @PostMapping("/send_invite/{user_id}/{chat_id}")
    @ResponseStatus(HttpStatus.OK)
    public ChatInvitation sendChatInvite(@AuthenticationPrincipal User user,
                                                 @PathVariable UUID user_id,
                                                 @PathVariable UUID chat_id)
    {
        return chatAndMessageService.sendGroupChatInvitation(user, user_id, chat_id);

    }


}

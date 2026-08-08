package com.rebuild.backend.controllers.forum_controllers;

import com.rebuild.backend.model.dtos.forum_dtos.message_and_chat_dtos.MessageDisplayDTO;
import com.rebuild.backend.model.entities.chat_entities.ChatInvitation;
import com.rebuild.backend.model.entities.chat_entities.GroupChat;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.forms.chat_forms.NewChatForm;
import com.rebuild.backend.model.responses.forum_responses.*;
import com.rebuild.backend.service.chat_services.ChatAdministrationService;
import com.rebuild.backend.service.chat_services.MessageService;
import com.rebuild.backend.service.chat_services.ChatService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chats")
@ResponseStatus(HttpStatus.OK)
public class ChatsController {

    private final ChatService chatService;

    @Autowired
    public ChatsController(ChatService chatService) {
        this.chatService = chatService;
    }


    @GetMapping("/all_chats")
    @ResponseStatus(HttpStatus.OK)
    public List<DisplayChatResponse> showAllChats(@AuthenticationPrincipal User authenticatedUser) {
        return chatService.displayAllChats(authenticatedUser);
    }

    @GetMapping("/load/{chat_id}")
    @ResponseStatus(HttpStatus.OK)
    public LoadChatResponse loadChat(@PathVariable UUID chat_id,
                                     @AuthenticationPrincipal User authenticatedUser,
                                     @RequestParam(name = "page", defaultValue = "0") int pageNumber) {
        return chatService.loadChat(chat_id, authenticatedUser, pageNumber);
    }

    @PostMapping("/create")
    public GroupChat createGroupChat(@AuthenticationPrincipal User creatingUser,
                                     @Valid @RequestBody NewChatForm newChatForm)
    {
        return chatService.createNewGroupChat(creatingUser, newChatForm);
    }


    @DeleteMapping("/reject_invite/{invitation_id}")
    public void rejectChatInvitation(@AuthenticationPrincipal User rejectingUser, @PathVariable UUID invitation_id)
    {
        chatService.declineChatInvitation(rejectingUser, invitation_id);
    }

    @DeleteMapping("/reject_all_invites")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rejectAllInvitations(@AuthenticationPrincipal User user)
    {
        chatService.declineAllChatInvitations(user);
    }

    @PostMapping("/accept_invite/{invitation_id}")
    @ResponseStatus(HttpStatus.OK)
    public GroupChat acceptChatInvitation(@AuthenticationPrincipal User acceptingUser,
                                                          @PathVariable UUID invitation_id) {
        return chatService.acceptChatInvitation(acceptingUser, invitation_id);
    }

    @PostMapping("/accept_all_invites")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void acceptAllInvitations(@AuthenticationPrincipal User user)
    {
        chatService.acceptAllChatInvitations(user);
    }


    @GetMapping("/get_all_ids")
    @ResponseStatus(HttpStatus.OK)
    public List<UUID> gatherChatIds(@AuthenticationPrincipal User user)
    {
        return chatService.findAllChatIdsByUser(user);
    }

    @PostMapping("/toggle_chat_mute/{chat_id}")
    @ResponseStatus(HttpStatus.OK)
    public boolean toggleChatMute(@AuthenticationPrincipal User user, @PathVariable UUID chat_id)
    {
        return chatService.toggleChatMute(user, chat_id);
    }


    @DeleteMapping("/kick_user/{chat_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leaveChat(@AuthenticationPrincipal User user, @PathVariable UUID chat_id)
    {
        chatService.leaveChat(user, chat_id);
    }

    @GetMapping("/load_users/{chat_id}")
    @ResponseStatus(HttpStatus.OK)
    public LoadChatUsersResponse loadUsers(@AuthenticationPrincipal User user,
                                           @PathVariable UUID chat_id)
    {
        return chatService.loadChatUsers(user, chat_id);
    }

    @PostMapping("/apply/{chat_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendChatApplication(@AuthenticationPrincipal User user,
                                    @PathVariable UUID chat_id,
                                    @RequestBody String content)
    {
        chatService.applyToJoinChat(user, chat_id, content);
    }



}

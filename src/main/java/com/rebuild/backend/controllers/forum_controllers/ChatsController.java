package com.rebuild.backend.controllers.forum_controllers;

import com.rebuild.backend.model.dtos.forum_dtos.MessageDisplayDTO;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.ChatInvitation;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.GroupChat;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.forms.forum_forms.LoadMoreMessagesForm;
import com.rebuild.backend.model.responses.forum_responses.*;
import com.rebuild.backend.service.forum_services.ChatAdministrationService;
import com.rebuild.backend.service.forum_services.MessageService;
import com.rebuild.backend.service.forum_services.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/chats")
public class ChatsController {

    private final MessageService messageService;

    private final ChatService chatService;

    private final ChatAdministrationService administrationService;

    @Autowired
    public ChatsController(MessageService messageService, ChatService chatService, ChatAdministrationService administrationService) {
        this.messageService = messageService;
        this.chatService = chatService;
        this.administrationService = administrationService;
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

    @PostMapping("/send_message/{receiving_object_id}")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageDisplayDTO sendMessage(@PathVariable UUID receiving_object_id,
                                         @RequestBody String messageContent,
                                         @AuthenticationPrincipal User authenticatedUser) {
        return messageService.
                createMessage(authenticatedUser, receiving_object_id, messageContent);
    }

    @PostMapping("/create")
    public GroupChat createGroupChat(@AuthenticationPrincipal User creatingUser, @RequestBody String name)
    {
        return chatService.createNewGroupChat(creatingUser, name);
    }


    @PostMapping("/reject_invite/{invitation_id}")
    public void rejectChatInvitation(@AuthenticationPrincipal User rejectingUser, @PathVariable UUID invitation_id)
    {
        chatService.declineChatInvitation(rejectingUser, invitation_id);
    }

    @PostMapping("/accept_invite/{invitation_id}")
    @ResponseStatus(HttpStatus.OK)
    public GroupChat acceptChatInvitation(@AuthenticationPrincipal User acceptingUser,
                                                          @PathVariable UUID invitation_id) {
        return chatService.acceptChatInvitation(acceptingUser, invitation_id);
    }


    @PostMapping("/send_invite/{user_id}/{chat_id}")
    @ResponseStatus(HttpStatus.OK)
    public ChatInvitation sendChatInvite(@AuthenticationPrincipal User user,
                                                 @PathVariable UUID user_id,
                                                 @PathVariable UUID chat_id)
    {
        return administrationService.sendGroupChatInvitation(user, user_id, chat_id);

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
    

    @DeleteMapping("/remove_message/{message_id}")
    @ResponseStatus(HttpStatus.OK)
    public MessageDisplayDTO removeMessage(@AuthenticationPrincipal User user, @PathVariable UUID message_id){
        return messageService.removeMessage(user, message_id);
    }

    @PatchMapping("/edit_message/{message_id}")
    @ResponseStatus(HttpStatus.OK)
    public MessageDisplayDTO editMessage(@AuthenticationPrincipal User user,
                                         @PathVariable UUID message_id,
                                         @RequestBody String newMessage){
        return messageService.editMessage(user, message_id, newMessage);
    }

    @PatchMapping("/toggle_admin/{chat_id}/{user_id}")
    @ResponseStatus(HttpStatus.OK)
    public boolean toggleUserAdmin(@AuthenticationPrincipal User user,
                                   @PathVariable UUID chat_id, @PathVariable UUID user_id)
    {
        return administrationService.toggleUserAdmin(user, chat_id, user_id);
    }

    @DeleteMapping("/kick_user/{chat_id}/{user_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void kickUser(@AuthenticationPrincipal User user, @PathVariable UUID chat_id,
                         @PathVariable UUID user_id)
    {
        administrationService.kickUserFromChat(user, chat_id, user_id);
    }

    @DeleteMapping("/delete_chat/{chat_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteChat(@AuthenticationPrincipal User user, @PathVariable UUID chat_id)
    {

        administrationService.deleteChat(user, chat_id);
    }

    @DeleteMapping("/kick_user/{chat_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leaveChat(@AuthenticationPrincipal User user, @PathVariable UUID chat_id)
    {
        chatService.leaveChat(user, chat_id);
    }

    @DeleteMapping("/kick_user/{chat_id}/{user_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void transferChatOwnership(@AuthenticationPrincipal User user, @PathVariable UUID chat_id,
                         @PathVariable UUID user_id)
    {
        administrationService.transferChatOwnership(user, chat_id, user_id);
    }

    @GetMapping("/search_messages/{chat_id}")
    @ResponseStatus(HttpStatus.OK)
    public SearchMessagesResponse doMessageSearch(@AuthenticationPrincipal User user,
                                                  @PathVariable UUID chat_id,
                                                  @RequestBody String searchString,
                                                  @RequestParam(name = "page", defaultValue = "0") int pageNumber){
        return messageService.searchForMessages(user, chat_id, searchString, pageNumber);
    }

    @GetMapping("jump_to_message/{chat_id}/{message_id}")
    @ResponseStatus(HttpStatus.OK)
    public MessageJumpResponse jumpToMessage(@AuthenticationPrincipal User user,
                                             @PathVariable UUID chat_id, @PathVariable UUID message_id){
        return messageService.jumpToMessage(user, chat_id, message_id);
    }

    @GetMapping("jump_to_message/{chat_id}")
    @ResponseStatus(HttpStatus.OK)
    public LoadMoreMessagesResponse loadMoreMessages(@AuthenticationPrincipal User user,
                                                     @PathVariable UUID chat_id,
                                                     @RequestBody LoadMoreMessagesForm moreMessagesForm)
    {
        return messageService.loadMoreMessagesWithTimestamp(user, chat_id,
                moreMessagesForm.lastTimestamp(),  moreMessagesForm.loadFromAbove());
    }

    @GetMapping("/load_users/{chat_id}")
    @ResponseStatus(HttpStatus.OK)
    public LoadChatUsersResponse loadUsers(@AuthenticationPrincipal User user,
                                           @PathVariable UUID chat_id)
    {
        return chatService.loadChatUsers(user, chat_id);
    }

    @PatchMapping("/pin_or_unpin/{chat_id}/{message_id}")
    @ResponseStatus(HttpStatus.OK)
    public boolean changePinnedStatus(@AuthenticationPrincipal User user,
                                      @PathVariable UUID chat_id, @PathVariable UUID message_id)
    {
        return administrationService.pinOrUnpinMessage(user, chat_id, message_id);
    }

    @GetMapping("/pinned_messages/{chat_id}")
    public PinnedMessagesResponse getPinnedMessages(@AuthenticationPrincipal User user,
                                                    @PathVariable UUID chat_id,
                                                    @RequestParam(name = "page", defaultValue = "0")  int pageNumber)
    {
        return messageService.getPinnedMessages(user, chat_id, pageNumber);
    }



}

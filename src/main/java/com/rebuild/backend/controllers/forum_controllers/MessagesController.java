package com.rebuild.backend.controllers.forum_controllers;

import com.rebuild.backend.model.dtos.forum_dtos.message_and_chat_dtos.MessageDisplayDTO;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.responses.forum_responses.LoadMoreMessagesResponse;
import com.rebuild.backend.model.responses.forum_responses.PinnedMessagesResponse;
import com.rebuild.backend.model.responses.forum_responses.SearchMessagesResponse;
import com.rebuild.backend.service.chat_services.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
@ResponseStatus(HttpStatus.OK)
public class MessagesController {

    private final MessageService messageService;

    @Autowired
    public MessagesController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/send_message/private_chat/{chat_id}")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageDisplayDTO sendMessageToPrivateChat(@PathVariable UUID chat_id,
                                         @RequestBody String messageContent,
                                         @AuthenticationPrincipal User authenticatedUser) {
        return messageService.sendMessageInPrivateChat(authenticatedUser,  chat_id, messageContent);
    }

    @PostMapping("/send_message/group_chat/{chat_id}")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageDisplayDTO sendMessageToGroupChat(@PathVariable UUID chat_id,
                                                      @RequestBody String messageContent,
                                                      @AuthenticationPrincipal User authenticatedUser) {
        return messageService.sendMessageInGroupChat(authenticatedUser,  chat_id, messageContent);
    }

    @PostMapping("/send_message/user/{user_id}")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageDisplayDTO sendMessageToAnotherUser(@PathVariable UUID user_id,
                                                      @RequestBody String messageContent,
                                                      @AuthenticationPrincipal User authenticatedUser) {
        return messageService.sendMessageToUser(authenticatedUser, user_id, messageContent);
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

    @GetMapping("/search_messages/{chat_id}")
    @ResponseStatus(HttpStatus.OK)
    public SearchMessagesResponse doMessageSearch(@AuthenticationPrincipal User user,
                                                  @PathVariable UUID chat_id,
                                                  @RequestBody String searchString,
                                                  @RequestParam(name = "page", defaultValue = "0") int pageNumber){
        return messageService.searchForMessages(user, chat_id, searchString, pageNumber);
    }

    @GetMapping("jump_to_message/{chat_id}")
    @ResponseStatus(HttpStatus.OK)
    public LoadMoreMessagesResponse loadMoreMessages(@AuthenticationPrincipal User user,
                                                     @PathVariable UUID chat_id,
                                                     @RequestParam(name = "page", defaultValue = "0") int pageNumber)
    {
        return messageService.loadMoreMessages(user, chat_id, pageNumber);
    }

    @GetMapping("/pinned_messages/{chat_id}")
    @ResponseStatus(HttpStatus.OK)
    public PinnedMessagesResponse getPinnedMessages(@AuthenticationPrincipal User user,
                                                    @PathVariable UUID chat_id,
                                                    @RequestParam(name = "page", defaultValue = "0")  int pageNumber)
    {
        return messageService.getPinnedMessages(user, chat_id, pageNumber);
    }
}

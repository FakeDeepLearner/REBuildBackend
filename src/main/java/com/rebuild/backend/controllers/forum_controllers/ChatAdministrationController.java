package com.rebuild.backend.controllers.forum_controllers;

import com.rebuild.backend.model.entities.chat_entities.ChatInvitation;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.responses.user_responses.ChatApplicationSearchResponse;
import com.rebuild.backend.service.chat_services.ChatAdministrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/chats/admin")
@ResponseStatus(HttpStatus.OK)
public class ChatAdministrationController {

    private final ChatAdministrationService administrationService;

    @Autowired
    public ChatAdministrationController(ChatAdministrationService administrationService) {
        this.administrationService = administrationService;
    }

    @PostMapping("/send_invite/{user_id}/{chat_id}")
    @ResponseStatus(HttpStatus.OK)
    public ChatInvitation sendChatInvite(@AuthenticationPrincipal User user,
                                         @PathVariable UUID user_id,
                                         @PathVariable UUID chat_id)
    {
        return administrationService.sendGroupChatInvitation(user, user_id, chat_id);
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

    @PatchMapping("/pin_or_unpin/{chat_id}/{message_id}")
    @ResponseStatus(HttpStatus.OK)
    public boolean changePinnedStatus(@AuthenticationPrincipal User user,
                                      @PathVariable UUID chat_id, @PathVariable UUID message_id)
    {
        return administrationService.pinOrUnpinMessage(user, chat_id, message_id);
    }

    @PatchMapping("/update_status/{chat_id}")
    @ResponseStatus(HttpStatus.OK)
    public String updateChatStatus(@AuthenticationPrincipal User user,
                                   @PathVariable UUID chat_id,
                                   @RequestBody String newStatus){
        return administrationService.updateChatStatus(user, chat_id, newStatus);
    }

    @GetMapping("/get_applications/{chat_id}")
    public ChatApplicationSearchResponse getAllApplications(@AuthenticationPrincipal User user,
                                                            @PathVariable UUID chat_id,
                                                            @RequestParam(defaultValue = "0", name = "page", required = false)
                                                                int pageNumber)
    {
        return administrationService.seeChatApplications(user, chat_id, pageNumber);
    }

    @PostMapping("/accept_application/{chat_id}/{application_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void acceptChatApplication(@AuthenticationPrincipal User user,
                                      @PathVariable UUID chat_id, @PathVariable UUID application_id)
    {
        administrationService.acceptChatApplication(user, chat_id, application_id);
    }

    @PostMapping("/accept_all_applications/{chat_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void acceptAllChatApplications(@AuthenticationPrincipal User user,
                                      @PathVariable UUID chat_id)
    {
        administrationService.acceptAllApplications(user, chat_id);
    }

    @DeleteMapping("/reject_application/{chat_id}/{application_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rejectChatApplication(@AuthenticationPrincipal User user,
                                      @PathVariable UUID chat_id, @PathVariable UUID application_id)
    {
        administrationService.rejectChatApplication(user, chat_id, application_id);
    }

    @DeleteMapping("/reject_all_applications/{chat_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rejectAllApplications(@AuthenticationPrincipal User user,
                                      @PathVariable UUID chat_id)
    {
        administrationService.rejectAllApplications(user, chat_id);
    }


}

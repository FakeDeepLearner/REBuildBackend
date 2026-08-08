package com.rebuild.backend.controllers;

import com.rebuild.backend.model.dtos.user_dtos.UserFriendDTO;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.forms.profile_forms.ProfilePrivacySettingsForm;
import com.rebuild.backend.model.responses.user_responses.ChatApplicationSearchResponse;
import com.rebuild.backend.model.responses.user_responses.FriendRequestResponse;
import com.rebuild.backend.model.responses.user_responses.UserProfileResponse;
import com.rebuild.backend.model.responses.user_responses.UsernameSearchResponse;
import com.rebuild.backend.service.chat_services.ChatService;
import com.rebuild.backend.service.user_services.FriendshipService;
import com.rebuild.backend.service.user_services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@ResponseStatus(HttpStatus.OK)
public class UserController {

    private final UserService userService;

    private final FriendshipService friendshipService;

    private final ChatService chatService;


    @Autowired
    public UserController(UserService userService, FriendshipService friendshipService,
                          ChatService chatService) {
        this.userService = userService;
        this.friendshipService = friendshipService;
        this.chatService = chatService;
    }


    @GetMapping("/load_profile")
    public UserProfileResponse loadOwnProfile(@AuthenticationPrincipal User user)
    {
        return userService.loadSelfProfile(user);
    }

    @GetMapping("/load_profile/{clicked_user_id}")
    public UserProfileResponse loadClickedUserProfile(@AuthenticationPrincipal User user, @PathVariable UUID clicked_user_id)
    {
        return userService.loadUserProfile(user, clicked_user_id);
    }

    @PatchMapping("/update_privacy_settings")
    @ResponseStatus(HttpStatus.OK)
    public UserProfileResponse updateProfilePrivacy(@AuthenticationPrincipal User user,
                                                    @RequestBody ProfilePrivacySettingsForm privacySettingsForm)
    {
        return userService.changeProfilePrivacySettings(user, privacySettingsForm);
    }

    @PatchMapping("/update_location")
    @ResponseStatus(HttpStatus.OK)
    public String updateUserLocation(@AuthenticationPrincipal User user,
                                     @RequestBody String newLocation)
    {
        return userService.updateUserLocation(user, newLocation);
    }


    @PatchMapping("/update_biography")
    @ResponseStatus(HttpStatus.OK)
    public String updateUserBiography(@AuthenticationPrincipal User user,
                                     @RequestBody String newBiography)
    {
        return userService.updateUserBiography(user, newBiography);
    }

    @GetMapping("/friends/received_requests")
    @ResponseStatus(HttpStatus.OK)
    public List<FriendRequestResponse> loadReceivedFriendRequests(@AuthenticationPrincipal User authenticatedUser) {
        return friendshipService.loadReceivedFriendRequests(authenticatedUser);
    }

    @GetMapping("/friends/sent_requests")
    @ResponseStatus(HttpStatus.OK)
    public List<FriendRequestResponse> loadSentFriendRequests(@AuthenticationPrincipal User authenticatedUser) {
        return friendshipService.loadSentFriendRequests(authenticatedUser);
    }

    @GetMapping("/friends")
    @ResponseStatus(HttpStatus.OK)
    public List<UserFriendDTO> loadFriends(@AuthenticationPrincipal User authenticatedUser) {
        return friendshipService.getUserFriends(authenticatedUser);
    }

    @PostMapping("/accept_request/{request_id}")
    public ResponseEntity<String> acceptFriendshipRequest(@PathVariable UUID request_id,
                                                          @AuthenticationPrincipal User acceptingUser) {
        String result = friendshipService.acceptFriendshipRequest(acceptingUser, request_id);
        return ResponseEntity.ok("You have added " + result + " as a friend");

    }

    @PostMapping("/decline_request/{request_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void declineFriendshipRequest(@PathVariable UUID request_id,
                                         @AuthenticationPrincipal User acceptingUser) {
        friendshipService.declineFriendshipRequest(acceptingUser, request_id);
    }

    @DeleteMapping("/delete_request/{request_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelFriendshipRequest(@PathVariable UUID request_id,
                                        @AuthenticationPrincipal User deletingUser) {
        friendshipService.cancelFriendshipRequest(deletingUser, request_id);
    }

    @PostMapping("/send_friendship/{recipient_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendFriendshipRequest(@PathVariable UUID recipient_id,
                                      @AuthenticationPrincipal User sendingUser,
                                      @RequestBody String content)
    {
        friendshipService.sendFriendRequest(sendingUser, recipient_id, content);
    }

    @GetMapping("/username_search")
    public UsernameSearchResponse searchUsernames(@RequestBody String username,
                                                  @AuthenticationPrincipal User user,
                                                  @RequestParam(defaultValue = "0", name = "page", required = false)
                                                  int pageNumber)
    {
        return userService.getUsernameSearchResults(username, user, pageNumber);
    }


    @GetMapping("/chat_applications")
    public ChatApplicationSearchResponse getAllChatApplications(@AuthenticationPrincipal User user,
                                                                @RequestParam(defaultValue = "0", name = "page", required = false)
                                                                int pageNumber)
    {
        return userService.getAllChatApplications(user, pageNumber);
    }


    @DeleteMapping("/cancel_application/{application_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelChatApplication(@AuthenticationPrincipal User user,
                                    @PathVariable UUID application_id)
    {
        chatService.cancelChatApplication(user, application_id);
    }

    @DeleteMapping("/cancel_all_applications")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelAllApplications(@AuthenticationPrincipal User user)
    {
        chatService.cancelAllChatApplications(user);
    }

}

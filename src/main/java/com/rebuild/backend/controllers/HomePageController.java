package com.rebuild.backend.controllers;

import com.rebuild.backend.model.dtos.forum_dtos.NewMessageDTO;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.Chat;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.Message;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.dtos.forum_dtos.UsernameSearchResultDTO;
import com.rebuild.backend.model.responses.DisplayChatResponse;
import com.rebuild.backend.model.responses.LoadChatResponse;
import com.rebuild.backend.model.responses.HomePageData;
import com.rebuild.backend.service.forum_services.ChatAndMessageService;
import com.rebuild.backend.service.forum_services.FriendshipService;
import com.rebuild.backend.service.resume_services.ResumeService;
import com.rebuild.backend.service.user_services.UserHomePageService;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;


@RestController
@RequestMapping("/home")
public class HomePageController {

    private final ResumeService resumeService;

    private final FriendshipService friendshipService;

    private final UserHomePageService homePageService;

    @Autowired
    public HomePageController(ResumeService resumeService, FriendshipService friendshipService,
                               UserHomePageService homePageService) {
        this.resumeService = resumeService;
        this.friendshipService = friendshipService;
        this.homePageService = homePageService;
    }

    @GetMapping("/resume/{resume_id}")
    @ResponseStatus(HttpStatus.OK)
    public Resume getResume(@AuthenticationPrincipal User user,
                            @PathVariable UUID resume_id){
        return resumeService.findByUserAndResumeId(user, resume_id);
    }

    @PostMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public HomePageData getHomePageWithForm(@AuthenticationPrincipal User authenticatedUser,
                                     @RequestParam(defaultValue = "0", name = "page") int pageNumber,
                                     @RequestParam(defaultValue = "10", name = "size") int pageSize,
                                     @RequestBody String nameToSearch) {
        return homePageService.getSearchResult(nameToSearch,
                authenticatedUser, pageNumber, pageSize);
    }

    @GetMapping("/")
    @ResponseStatus(HttpStatus.OK)
    public HomePageData getDefaultHomePage(@AuthenticationPrincipal User authenticatedUser,
                                     @RequestParam(defaultValue = "0", name = "page") int pageNumber,
                                     @RequestParam(defaultValue = "10", name = "size") int pageSize) {
        return homePageService.getHomePageData(authenticatedUser, pageNumber, pageSize);
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createNewResume(@RequestBody String name,
                                          @AuthenticationPrincipal User authenticatedUser) {
        try{
            Resume createdResume = resumeService.createNewResumeFor(name, authenticatedUser);

            return ResponseEntity.status(HttpStatus.CREATED).body(createdResume);
        }
        catch (DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            if (cause instanceof ConstraintViolationException violationException &&
                    Objects.equals(violationException.getConstraintName(), "uk_same_user_resume_name")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("You already have a resume with this name");
            }
        }
        catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(e.getMessage());
        }

        //Should never get here.
        return null;
    }

    @GetMapping("/friends")
    @ResponseStatus(HttpStatus.OK)
    public List<UsernameSearchResultDTO> loadFriendRequests(@AuthenticationPrincipal User authenticatedUser) {
        return friendshipService.loadUserFriendRequests(authenticatedUser);
    }

}

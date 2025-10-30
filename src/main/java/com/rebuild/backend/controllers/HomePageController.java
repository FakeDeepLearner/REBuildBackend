package com.rebuild.backend.controllers;

import com.rebuild.backend.model.entities.messaging_and_friendship_entities.Message;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.resume_entities.ResumeSearchConfiguration;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.forms.resume_forms.ResumeSpecsForm;
import com.rebuild.backend.model.responses.DisplayChatResponse;
import com.rebuild.backend.model.responses.LoadChatResponse;
import com.rebuild.backend.model.responses.HomePageData;
import com.rebuild.backend.repository.resume_repositories.ResumeSearchRepository;
import com.rebuild.backend.service.forum_services.FriendAndMessageService;
import com.rebuild.backend.service.resume_services.ResumeService;
import com.rebuild.backend.service.user_services.UserService;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

@RestController
public class HomePageController {

    private final UserService userService;

    private final ResumeService resumeService;

    private final ResumeSearchRepository searchRepository;

    private final FriendAndMessageService friendAndMessageService;

    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public HomePageController(UserService userService, ResumeService resumeService,
                              ResumeSearchRepository searchRepository, FriendAndMessageService friendAndMessageService, SimpMessagingTemplate simpMessagingTemplate) {
        this.userService = userService;
        this.resumeService = resumeService;
        this.searchRepository = searchRepository;
        this.friendAndMessageService = friendAndMessageService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @PostMapping("/get_posts/configuration/{config_id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getPosts(@AuthenticationPrincipal User user,
                                      @PathVariable UUID config_id) {
        try {

            ResumeSearchConfiguration foundConfig = searchRepository.findById(config_id).get();

            ResumeSpecsForm craftedBody = resumeService.createSpecsForm(foundConfig);

            HomePageData response = userService.getSearchResult(craftedBody, user,
                            0, 20);

            return ResponseEntity.ok(response);
        }
        catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping( "home/create_resume_search_config")
    @ResponseStatus(HttpStatus.CREATED)
    public ResumeSearchConfiguration createSearchConfig(@AuthenticationPrincipal User authenticatedUser,
                                                        @RequestBody ResumeSpecsForm specsForm)
    {
        return resumeService.createSearchConfig(authenticatedUser, specsForm);
    }

    @GetMapping("/home/resume/{resume_id}")
    @ResponseStatus(HttpStatus.OK)
    public Resume getResume(@AuthenticationPrincipal User user,
                            @PathVariable UUID resume_id){
        return resumeService.findByUserIndex(user, resume_id);
    }

    @PostMapping("/home/search")
    @ResponseStatus(HttpStatus.OK)
    public HomePageData loadHomePage(@AuthenticationPrincipal User authenticatedUser,
                                     @RequestParam(defaultValue = "0", name = "page") int pageNumber,
                                     @RequestParam(defaultValue = "10", name = "size") int pageSize,
                                     @RequestBody ResumeSpecsForm specsForm) {
        return userService.getSearchResult(specsForm,
                authenticatedUser, pageNumber, pageSize);
    }


    @GetMapping("/chats/all_chats")
    public List<DisplayChatResponse> showAllChats(@AuthenticationPrincipal User authenticatedUser) {
        return friendAndMessageService.displayAllChats(authenticatedUser);
    }

    @GetMapping("/chats/load/{chat_id}")
    @ResponseStatus(HttpStatus.OK)
    public LoadChatResponse loadChat(@PathVariable UUID chat_id,
                                     @AuthenticationPrincipal User authenticatedUser) {
        return friendAndMessageService.loadChat(chat_id, authenticatedUser);
    }

    @PostMapping("/chats/send_message/{recipient_id}")
    public Message sendMessage(@PathVariable UUID recipient_id,
                                  @RequestBody String messageContent,
                                  @AuthenticationPrincipal User authenticatedUser) {
        Message createdMessage = friendAndMessageService.
                createMessage(authenticatedUser, recipient_id, messageContent);

        simpMessagingTemplate.convertAndSendToUser(recipient_id.toString(),
                "/messages", createdMessage);
        return createdMessage;


    }

    @GetMapping("/home")
    @ResponseStatus(HttpStatus.OK)
    public HomePageData loadHomePage(@AuthenticationPrincipal User authenticatedUser,
                                     @RequestParam(defaultValue = "0", name = "page") int pageNumber,
                                     @RequestParam(defaultValue = "10", name = "size") int pageSize,
                                     @RequestParam(name = "token", required = false) String searchToken) {
        return userService.getHomePageData(authenticatedUser, pageNumber, pageSize, searchToken);
    }

    @PostMapping("/api/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createNewResume(@RequestBody String name,
                                          @AuthenticationPrincipal User authenticatedUser) {
        try{
            Resume createdResume = resumeService.createNewResumeFor(name, authenticatedUser);

            return ResponseEntity.status(CREATED).body(createdResume);
        }
        catch (DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            if (cause instanceof ConstraintViolationException violationException &&
                    Objects.equals(violationException.getConstraintName(), "uk_same_user_resume_name")) {
                return ResponseEntity.status(CONFLICT).body("You already have a resume with this name");
            }
        }
        catch (RuntimeException e) {
            return ResponseEntity.status(PAYMENT_REQUIRED).body(e.getMessage());
        }

        //Should never get here.
        return null;
    }

    @DeleteMapping("/api/delete/{res_id}")
    @ResponseStatus(NO_CONTENT)
    public void deleteResume(@PathVariable UUID res_id){
        resumeService.deleteById(res_id);
    }


    @DeleteMapping("/api/delete_phone")
    @ResponseStatus(NO_CONTENT)
    public void removePhoneNumber(@AuthenticationPrincipal User authenticatedUser) {

        userService.removePhoneOf(authenticatedUser);
    }

    @PostMapping("/api/update_time_zone")
    @ResponseStatus(OK)
    public User updateTimeZone(@AuthenticationPrincipal User updatingUser,
                               @RequestBody String timeZone){
        return userService.modifyTimeZone(updatingUser, timeZone);
    }

}

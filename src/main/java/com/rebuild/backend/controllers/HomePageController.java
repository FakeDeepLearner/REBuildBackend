package com.rebuild.backend.controllers;

import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.dtos.forum_dtos.UsernameSearchResultDTO;
import com.rebuild.backend.model.responses.HomePageResponse;
import com.rebuild.backend.model.responses.resume_responses.ResumeResponse;
import com.rebuild.backend.service.forum_services.FriendshipService;
import com.rebuild.backend.service.resume_services.ResumeService;
import com.rebuild.backend.service.user_services.UserHomePageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public ResumeResponse getResume(@AuthenticationPrincipal User user,
                                    @PathVariable UUID resume_id){
        return resumeService.findByUserAndResumeId(user, resume_id);
    }

    @PostMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public HomePageResponse getHomePageWithForm(@AuthenticationPrincipal User authenticatedUser,
                                                @RequestParam(defaultValue = "0", name = "page") int pageNumber,
                                                @RequestBody String nameToSearch) {
        return homePageService.getSearchResult(nameToSearch,
                authenticatedUser, pageNumber);
    }

    @GetMapping("/")
    @ResponseStatus(HttpStatus.OK)
    public HomePageResponse getDefaultHomePage(@AuthenticationPrincipal User authenticatedUser,
                                               @RequestParam(defaultValue = "0", name = "page") int pageNumber) {
        return homePageService.getHomePageData(authenticatedUser, pageNumber);
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResumeResponse createNewResume(@RequestBody String name,
                                          @AuthenticationPrincipal User authenticatedUser) {
        return resumeService.createNewResumeFor(name, authenticatedUser);
    }

    @GetMapping("/friends")
    @ResponseStatus(HttpStatus.OK)
    public List<UsernameSearchResultDTO> loadFriendRequests(@AuthenticationPrincipal User authenticatedUser) {
        return friendshipService.loadUserFriendRequests(authenticatedUser);
    }

}

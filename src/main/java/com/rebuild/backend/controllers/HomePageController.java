package com.rebuild.backend.controllers;

import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.responses.HomePageData;
import com.rebuild.backend.service.resume_services.ResumeService;
import com.rebuild.backend.service.user_services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class HomePageController {

    private final UserService userService;

    private final ResumeService resumeService;

    @Autowired
    public HomePageController(UserService userService, ResumeService resumeService) {
        this.userService = userService;
        this.resumeService = resumeService;
    }

    @GetMapping("/home/user/{resume_id}")
    @ResponseStatus(HttpStatus.OK)
    public Resume getResume(@PathVariable UUID resume_id){
        return resumeService.findById(resume_id);
    }

    @GetMapping("/home")
    @ResponseStatus(HttpStatus.OK)
    public HomePageData loadHomePage(@AuthenticationPrincipal User authenticatedUser,
                                     @RequestParam(defaultValue = "0", name = "page") int pageNumber,
                                     @RequestParam(defaultValue = "10", name = "size") int pageSize) {
        return resumeService.loadHomePageInformation(authenticatedUser, pageNumber, pageSize);
    }

    @PostMapping("/api/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Resume createNewResume(@RequestBody String name,
                                  @AuthenticationPrincipal User authenticatedUser) {
        return resumeService.createNewResumeFor(name, authenticatedUser);
    }

    @DeleteMapping("/api/delete/{res_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteResume(@PathVariable UUID res_id){
        resumeService.deleteById(res_id);
    }


    @DeleteMapping("/api/delete_phone")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removePhoneNumber(@AuthenticationPrincipal User authenticatedUser) {

        userService.removePhoneOf(authenticatedUser);
    }

}

package com.rebuild.backend.controllers;

import com.rebuild.backend.exceptions.not_found_exceptions.UserNotFoundException;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.User;
import com.rebuild.backend.model.responses.GetHomePageResponse;
import com.rebuild.backend.service.ResumeService;
import com.rebuild.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @GetMapping("/home/{user_id}")
    @ResponseStatus(HttpStatus.OK)
    public GetHomePageResponse getAllResumes(@PathVariable UUID user_id){
        User associatedUser = userService.findByID(user_id).
                orElseThrow(() -> new UserNotFoundException("User not found"));
        return new GetHomePageResponse(associatedUser.getResumes(), associatedUser.getProfile());
    }

    @PostMapping("/api/{user_id}/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Resume createNewResume(@PathVariable UUID user_id){
        User creatingUser = userService.findByID(user_id).
                orElseThrow(() -> new UserNotFoundException("User not found with the given id"));
        return resumeService.createNewResumeFor(creatingUser);
    }

    @DeleteMapping("/api/delete/{res_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteResume(@PathVariable UUID res_id){
        resumeService.deleteById(res_id);
    }


    @DeleteMapping("/api/delete_phone/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removePhoneNumber(@PathVariable UUID id){
        userService.removePhoneOf(id);
    }

}

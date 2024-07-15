package com.rebuild.backend.controllers;

import com.rebuild.backend.exceptions.not_found_exceptions.UserNotFoundException;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.User;
import com.rebuild.backend.model.forms.EmailChangeForm;
import com.rebuild.backend.service.ResumeService;
import com.rebuild.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.Optional;
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


    @GetMapping("/home/{user_id}")
    @ResponseStatus(HttpStatus.OK)
    public List<Resume> getAllResumes(@PathVariable UUID user_id){
        return userService.getAllResumesById(user_id);
    }

    @PostMapping("/api/{user_id}/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Resume createNewResume(@PathVariable UUID user_id){
        User creatingUser = userService.findByID(user_id).
                orElseThrow(() -> new UserNotFoundException("User not found with the given id"));

        Resume newResume = new Resume(creatingUser);
        return resumeService.save(newResume);
    }

}

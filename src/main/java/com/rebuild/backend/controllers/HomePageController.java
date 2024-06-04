package com.rebuild.backend.controllers;

import com.rebuild.backend.exceptions.UserNotFoundException;
import com.rebuild.backend.model.entities.Resume;
import com.rebuild.backend.model.entities.User;
import com.rebuild.backend.service.ResumeService;
import com.rebuild.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public List<Resume> getAllResumes(@PathVariable UUID user_id){
        return userService.getAllResumesById(user_id);
    }

    @PostMapping("/api/{user_id}/create")
    public Resume createNewResume(@PathVariable UUID user_id){
        Optional<User> creatingUser = userService.findByID(user_id);
        if(creatingUser.isEmpty()){
            throw new UserNotFoundException("User not found with the given ID");
        }
        User actualUser = creatingUser.get();
        Resume newResume = new Resume(actualUser);
        return resumeService.save(newResume);
    }




}

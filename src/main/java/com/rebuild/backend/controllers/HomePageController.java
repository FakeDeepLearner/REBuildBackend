package com.rebuild.backend.controllers;

import com.rebuild.backend.model.entities.Resume;
import com.rebuild.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class HomePageController {

    private final UserService userService;

    @Autowired
    public HomePageController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/home/{user_id}")
    public List<Resume> getAllResumes(@PathVariable UUID user_id){
        return userService.getAllResumesById(user_id);
    }


}

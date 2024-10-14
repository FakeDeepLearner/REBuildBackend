package com.rebuild.backend.controllers.forum_controllers;

import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.forms.forum_forms.ForumSignupForm;
import com.rebuild.backend.service.user_services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/forum/auth")
@RestController
public class ForumAuthenticationController {

    private final UserService userService;

    @Autowired
    public ForumAuthenticationController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.OK)
    public User signUpToForum(@Valid @RequestBody ForumSignupForm signupForm,
                              @AuthenticationPrincipal User authenticatedUser) {
        return userService.signUserUpToForum(signupForm.username(), signupForm.password(), authenticatedUser);
    }
}

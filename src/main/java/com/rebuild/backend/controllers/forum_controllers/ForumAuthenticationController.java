package com.rebuild.backend.controllers.forum_controllers;

import com.rebuild.backend.config.properties.AppUrlBase;
import com.rebuild.backend.exceptions.conflict_exceptions.InvalidForumCredentialsException;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.forms.dtos.error_dtos.OptionalValueAndErrorResult;
import com.rebuild.backend.model.forms.forum_forms.ForumLoginForm;
import com.rebuild.backend.model.forms.forum_forms.ForumSignupForm;
import com.rebuild.backend.model.responses.ForumPostPageResponse;
import com.rebuild.backend.service.forum_services.ForumAuthenticationService;
import com.rebuild.backend.service.user_services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RequestMapping("/api/forum/auth")
@RestController
public class ForumAuthenticationController {

    private final UserService userService;

    private final ForumAuthenticationService forumAuthenticationService;
    private final AppUrlBase urlBase;

    @Autowired
    public ForumAuthenticationController(UserService userService, ForumAuthenticationService forumAuthenticationService, AppUrlBase urlBase) {
        this.userService = userService;
        this.forumAuthenticationService = forumAuthenticationService;
        this.urlBase = urlBase;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.SEE_OTHER)
    public ResponseEntity<Void> signUpToForum(@Valid @RequestBody ForumSignupForm signupForm,
                              @AuthenticationPrincipal User authenticatedUser) {
        forumAuthenticationService.signUserUpToForum(signupForm.username(),
                signupForm.password(), authenticatedUser);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(urlBase.baseUrl() + "/api/forum/get_posts?size=" +
                authenticatedUser.getProfile().getForumPageSize()));
        return ResponseEntity.status(303).headers(headers).build();
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.SEE_OTHER)
    public ResponseEntity<Void> loginToForum(@Valid @RequestBody ForumLoginForm forumLoginForm,
                                                              @AuthenticationPrincipal User authenticatedUser) {
        forumAuthenticationService.validateForumCredentials(authenticatedUser, forumLoginForm);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(urlBase.baseUrl() + "/api/forum/get_posts?size=" +
                authenticatedUser.getProfile().getForumPageSize()));
        return ResponseEntity.status(303).headers(headers).build();
    }

    @PostMapping("/change_username")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> changeUsername(@AuthenticationPrincipal User authenticatedUser,
                               @RequestBody String newUsername){
        OptionalValueAndErrorResult<User> changingResult =
                userService.modifyForumUsername(authenticatedUser, newUsername);
        if(changingResult.optionalResult().isPresent()) {
            return ResponseEntity.ok(changingResult.optionalResult().get());
        }
        if(changingResult.optionalError().isEmpty()){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
        else{
            throw new InvalidForumCredentialsException(changingResult.optionalError().get());
        }

    }

    @PostMapping("/change_password")
    @ResponseStatus(HttpStatus.OK)
    public User changePassword(@AuthenticationPrincipal User authenticatedUser,
                               @RequestBody String newRawPassword){
        return userService.modifyForumPassword(authenticatedUser, newRawPassword);
    }
}

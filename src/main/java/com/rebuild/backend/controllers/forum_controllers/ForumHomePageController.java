package com.rebuild.backend.controllers.forum_controllers;

import com.rebuild.backend.exceptions.conflict_exceptions.InvalidForumCredentialsException;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.forms.dtos.forum_dtos.ForumSpecsDTO;
import com.rebuild.backend.model.forms.dtos.forum_dtos.PostDisplayDTO;
import com.rebuild.backend.model.responses.ForumPostPageResponse;
import com.rebuild.backend.service.forum_services.ForumPostAndCommentService;
import com.rebuild.backend.service.user_services.UserService;
import com.rebuild.backend.utils.OptionalValueAndErrorResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/forum")
public class ForumHomePageController {

    private final ForumPostAndCommentService postAndCommentService;

    private final UserService userService;

    @Autowired
    public ForumHomePageController(ForumPostAndCommentService postAndCommentService,
                                   UserService userService) {
        this.postAndCommentService = postAndCommentService;
        this.userService = userService;
    }

    @GetMapping(value = "/get_posts")
    @ResponseStatus(HttpStatus.OK)
    public ForumPostPageResponse getPosts(@RequestParam(defaultValue = "0", name = "page")
                                          int pageNumber,

                                          @RequestParam(defaultValue = "20", name = "size")
                                          int pageSize,

                                          @ModelAttribute ForumSpecsDTO forumSpecsDTO, BindingResult result) {

        return postAndCommentService.getPageResponses(pageNumber, pageSize, forumSpecsDTO);
    }

    @GetMapping("/get_posts/{post_id}")
    @ResponseStatus(HttpStatus.OK)
    public PostDisplayDTO loadPost(@PathVariable UUID post_id) {
        return postAndCommentService.loadPost(post_id);
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
}

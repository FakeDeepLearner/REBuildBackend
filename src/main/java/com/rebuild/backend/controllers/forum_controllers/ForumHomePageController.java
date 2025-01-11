package com.rebuild.backend.controllers.forum_controllers;

import com.rebuild.backend.model.forms.dtos.forum_dtos.ForumSpecsDTO;
import com.rebuild.backend.model.responses.ForumPostPageResponse;
import com.rebuild.backend.service.forum_services.ForumPostAndCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/forum")
public class ForumHomePageController {

    private final ForumPostAndCommentService postAndCommentService;

    @Autowired
    public ForumHomePageController(ForumPostAndCommentService postAndCommentService) {
        this.postAndCommentService = postAndCommentService;
    }

    @GetMapping("/get_posts")
    @ResponseStatus(HttpStatus.OK)
    public ForumPostPageResponse getPosts(@RequestParam(defaultValue = "0", name = "page")
                                          int pageNumber,

                                          @RequestParam(defaultValue = "20", name = "size")
                                          int pageSize,

                                          @RequestBody ForumSpecsDTO forumSpecsDTO) {

        return postAndCommentService.getPageResponses(pageNumber, pageSize, forumSpecsDTO);
    }
}

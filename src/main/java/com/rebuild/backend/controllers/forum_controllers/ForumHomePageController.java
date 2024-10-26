package com.rebuild.backend.controllers.forum_controllers;

import com.rebuild.backend.model.responses.ForumPostPageResponse;
import com.rebuild.backend.service.forum_services.ForumPostAndCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
    public ForumPostPageResponse getPosts(@RequestParam(defaultValue = "0", name = "page") int pageNumber,
                                          @RequestParam(defaultValue = "20", name = "size") int pageSize) {

        return postAndCommentService.getPageResponses(pageNumber, pageSize);
    }
}

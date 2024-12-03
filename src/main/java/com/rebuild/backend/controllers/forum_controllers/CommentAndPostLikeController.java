package com.rebuild.backend.controllers.forum_controllers;

import com.rebuild.backend.service.forum_services.CommentAndPostLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/forum/like")
public class CommentAndPostLikeController {

    private final CommentAndPostLikeService commentAndPostLikeService;


    @Autowired
    public CommentAndPostLikeController(CommentAndPostLikeService commentAndPostLikeService) {
        this.commentAndPostLikeService = commentAndPostLikeService;
    }

}

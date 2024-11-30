package com.rebuild.backend.service.forum_services;

import com.rebuild.backend.repository.CommentLikeRepository;
import com.rebuild.backend.repository.PostLikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommentAndPostLikeService {

    private final CommentLikeRepository commentLikeRepository;

    private final PostLikeRepository postLikeRepository;

    @Autowired
    public CommentAndPostLikeService(CommentLikeRepository commentLikeRepository,
                                     PostLikeRepository postLikeRepository) {
        this.commentLikeRepository = commentLikeRepository;
        this.postLikeRepository = postLikeRepository;
    }





}

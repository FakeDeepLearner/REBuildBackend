package com.rebuild.backend.model.forms.dtos.forum_dtos;

import com.rebuild.backend.model.entities.forum_entities.PostResume;

import java.util.List;

public record PostDisplayDTO(String title, String content,
                             String authorUsername, List<PostResume> resumes,
                             List<CommentDisplayDTO> displayedComments) {
}

package com.rebuild.backend.model.forms.dtos.forum_dtos;

import java.util.List;

public record PostDisplayDTO(String title, String content,
                             String authorUsername, List<CommentDisplayDTO> displayedComments) {
}

package com.rebuild.backend.model.forms.forum_forms;

import jakarta.validation.constraints.NotBlank;

public record CommentForm(@NotBlank(message = "Content can't be empty") String content,
                          boolean remainAnonymous) {
}

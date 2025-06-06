package com.rebuild.backend.model.forms.forum_forms;

import jakarta.validation.constraints.NotEmpty;

public record NewPostForm(@NotEmpty(message = "Title may not be empty") String title,
                          @NotEmpty(message = "Content may not be empty") String content,
                          boolean remainAnonymous) {
}

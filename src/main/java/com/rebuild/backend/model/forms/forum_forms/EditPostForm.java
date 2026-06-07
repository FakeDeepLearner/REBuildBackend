package com.rebuild.backend.model.forms.forum_forms;

import jakarta.validation.constraints.NotBlank;

public record EditPostForm(@NotBlank(message = "Title may not be empty") String newTitle,
                           @NotBlank(message = "Content may not be empty") String newContent) {
}

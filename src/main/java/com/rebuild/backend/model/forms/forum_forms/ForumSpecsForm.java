package com.rebuild.backend.model.forms.forum_forms;


import jakarta.validation.constraints.NotBlank;

public record ForumSpecsForm(@NotBlank(message = "Title may not be empty") String titleContains,
                             @NotBlank(message = "Body may not be empty") String bodyContains) {
}

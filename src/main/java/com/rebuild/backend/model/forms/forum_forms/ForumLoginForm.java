package com.rebuild.backend.model.forms.forum_forms;
//The reason we have 2 forms with the same data is because we will impose different constraints on them

public record ForumLoginForm(String username,
                             String password) {
}

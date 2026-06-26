package com.rebuild.backend.model.forms.profile_forms;

import jakarta.validation.constraints.NotBlank;

public record ProfilePrivacySettingsForm(@NotBlank(message = "Post visibility can't be empty")
                                         String postsVisibilityValue,
                                         @NotBlank(message = "Comment visibility cannot be empty")
                                         String commentsVisibilityValue,
                                         boolean messagesFromFriends,
                                         @NotBlank(message = "Sensitive info visibility can't be empty")
                                         String sensitiveInfoVisibilityValue) {

}

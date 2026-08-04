package com.rebuild.backend.model.forms.profile_forms;

import jakarta.validation.constraints.NotBlank;

public record ProfilePrivacySettingsForm(boolean messagesFromFriends,
                                         @NotBlank(message = "Sensitive info visibility can't be empty")
                                         String sensitiveInfoVisibilityValue) {

}

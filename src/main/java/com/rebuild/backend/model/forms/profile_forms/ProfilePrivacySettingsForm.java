package com.rebuild.backend.model.forms.profile_forms;

public record ProfilePrivacySettingsForm(String postsVisibilityValue,
                                         String commentsVisibilityValue,
                                         boolean messagesFromFriends,
                                         String sensitiveInfoVisibilityValue) {

}

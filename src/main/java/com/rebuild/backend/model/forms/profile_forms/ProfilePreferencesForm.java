package com.rebuild.backend.model.forms.profile_forms;

public record ProfilePreferencesForm(boolean publicPostHistory,
                                     boolean publicCommentHistory,
                                     boolean messagesFromFriendsOnly) {

}

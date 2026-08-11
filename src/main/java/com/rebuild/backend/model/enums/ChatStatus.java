package com.rebuild.backend.model.enums;

public enum ChatStatus {

    // Chat is visible and discoverable by all users, and any user can join without having to apply or be invited.
    // The chat cannot receive applications and cannot send invitations.
    ANYONE_CAN_JOIN("Anyone Can Join"),

    // Chat is still visible and discoverable, but only joinable via an
    // application or by being invited. This is also the default
    INVITE_OR_APPLICATION_ONLY("Invite or Application Only"),

    //Chat is not discoverable and can't receive applications. The only way to join is to be invited.
    CLOSED("Closed");

    public final String value;

    ChatStatus(String value)
    {
        this.value = value;
    }

    public static ChatStatus fromValue(String value){
        for (ChatStatus chatStatus : ChatStatus.values()) {
            if (chatStatus.value.equals(value)) {
                return chatStatus;
            }
        }
        return null;
    }
}

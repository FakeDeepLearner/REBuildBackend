package com.rebuild.backend.model.entities.messaging_and_friendship_entities;

import com.rebuild.backend.utils.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public enum ChatStatus {

    //Chat is visible and discoverable by all users, and any user can join without having to apply or be invited
    ANYONE_CAN_JOIN("Anyone Can Join"),

    // Chat is still visible and discoverable, but only joinable via an
    // application or by being invited. This is also the default
    INVITE_ONLY("Invite Only"),

    //Chat is not discoverable and can't receive applications. The only way to join is to be invited
    CLOSED("Closed");

    public final String value;

    ChatStatus(String value)
    {
        this.value = value;
    }

    public static ChatStatus fromValue(String value){
        return switch (value) {
            case "Anyone Can Join" -> ChatStatus.ANYONE_CAN_JOIN;
            case "Invite Only" -> ChatStatus.INVITE_ONLY;
            case "Closed" -> ChatStatus.CLOSED;
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid chat status value.");
        };

    }
}

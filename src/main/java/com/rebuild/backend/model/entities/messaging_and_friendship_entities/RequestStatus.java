package com.rebuild.backend.model.entities.messaging_and_friendship_entities;

public enum RequestStatus {

    PENDING,

    REJECTED,

    ACCEPTED,

    CANCELLED;  //Cancelled and abandoned by the sender
}

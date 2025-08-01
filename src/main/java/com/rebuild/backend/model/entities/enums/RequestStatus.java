package com.rebuild.backend.model.entities.enums;

public enum RequestStatus {

    PENDING,

    REJECTED,

    ACCEPTED,

    TIMED_OUT, //Timed out and no longer valid after a certain amount of time (TBD)

    CANCELLED;  //Cancelled and abandoned by the sender
}

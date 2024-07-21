package com.rebuild.backend.model.forms;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.temporal.ChronoUnit;

public record AccountActivationDTO(String email,
                                   String password,
                                   Long timeCount,
                                   @JsonProperty("unit")
                                           ChronoUnit timeUnit,
                                   boolean remember) {
}

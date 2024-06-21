package com.rebuild.backend.model.forms;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.temporal.ChronoUnit;

public record AccountActivationOrResetForm(String email, Long timeCount,
                                           @JsonProperty("unit")
                                           ChronoUnit timeUnit) {
}

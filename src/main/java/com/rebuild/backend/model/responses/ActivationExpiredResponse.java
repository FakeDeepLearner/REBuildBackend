package com.rebuild.backend.model.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ActivationExpiredResponse(String error_message,

                                        @JsonProperty("email")
                                        String failedEmailFor) {
}

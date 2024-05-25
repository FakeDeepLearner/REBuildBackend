package com.rebuild.backend.model.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthResponse(
        
        @JsonProperty("access")
        String accessToken,

        @JsonProperty("refresh")
        String refreshToken) {
}

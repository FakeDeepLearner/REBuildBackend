package com.rebuild.backend.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "password-rules")
public record PasswordCharacterAndNumberLimits(int consecutiveCharacterLimit, int consecutiveNumbersLimit) {
}

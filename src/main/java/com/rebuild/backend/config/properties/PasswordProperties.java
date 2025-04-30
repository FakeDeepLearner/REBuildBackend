package com.rebuild.backend.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "password-rules")
public record PasswordProperties(
                                 int minSize,
                                 int minUppercase,
                                 int minLowercase,
                                 int minDigit,
                                 int minSpecialCharacter,
                                 boolean canContainSpaces) {
}

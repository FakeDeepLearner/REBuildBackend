package com.rebuild.backend.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.security.mail-credentials")
public record MailAppCredentials(String address, String appPassword) {
}

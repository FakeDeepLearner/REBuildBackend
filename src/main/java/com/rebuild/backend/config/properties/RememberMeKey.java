package com.rebuild.backend.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("remember-me")
public record RememberMeKey(String key) {
}

package com.rebuild.backend.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "app")
public record AppUrlBase(String baseUrl) {
}

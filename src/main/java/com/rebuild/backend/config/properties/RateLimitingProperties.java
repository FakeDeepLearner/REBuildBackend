package com.rebuild.backend.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.security.rate-limiting")
public record RateLimitingProperties(int IpBlockHours, int UserBlockHours,
                                     int ipRequestLimit, int userRequestLimit) {
}

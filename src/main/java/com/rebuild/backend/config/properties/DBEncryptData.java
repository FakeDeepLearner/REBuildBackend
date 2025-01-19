package com.rebuild.backend.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "encryption")
public record DBEncryptData(String algorithm, String password, String salt) {
}

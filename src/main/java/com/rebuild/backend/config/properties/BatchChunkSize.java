package com.rebuild.backend.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("batch")
public record BatchChunkSize(int size) {
}

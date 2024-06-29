package com.rebuild.backend.config.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
public class TokenStorage {

    private final RedisConnectionFactory connectionFactory;

    @Autowired
    public TokenStorage(RedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

}

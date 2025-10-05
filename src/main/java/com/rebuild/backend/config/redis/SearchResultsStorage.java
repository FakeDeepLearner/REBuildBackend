package com.rebuild.backend.config.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
public class SearchResultsStorage {

    private final RedisConnectionFactory connectionFactory;

    @Autowired
    public SearchResultsStorage(RedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Bean
    public RedisCacheManager searchCacheManager()
    {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig().
                entryTtl(Duration.ofMinutes(2));

        return RedisCacheManager.builder(RedisCacheWriter.lockingRedisCacheWriter(connectionFactory)).
                cacheDefaults(config).
                withCacheConfiguration("search_cache", config).build();

    }
}

package com.rebuild.backend.config.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
public class RedisCachesConfig {

    private final RedisConnectionFactory connectionFactory;


    private final RedisResumeSerializer resumeSerializer;

    @Autowired
    public RedisCachesConfig(RedisConnectionFactory connectionFactory,
                             RedisResumeSerializer resumeSerializer) {
        this.connectionFactory = connectionFactory;
        this.resumeSerializer = resumeSerializer;
    }

    @Bean
    public RedisCacheManager cacheManager() {
        RedisCacheConfiguration searchResultsCacheConfig = RedisCacheConfiguration.defaultCacheConfig().
                entryTtl(Duration.ofMinutes(5)).enableTimeToIdle();

        RedisCacheConfiguration resumeCacheConfig = RedisCacheConfiguration.defaultCacheConfig().
                entryTtl(Duration.ofSeconds(30)).
                serializeValuesWith(RedisSerializationContext.
                        SerializationPair.
                        fromSerializer(resumeSerializer));

        RedisCacheConfiguration idempotencyConfig = RedisCacheConfiguration.defaultCacheConfig().
                entryTtl(Duration.ofSeconds(30));


        return RedisCacheManager.builder(RedisCacheWriter.lockingRedisCacheWriter(connectionFactory))
                .withCacheConfiguration("idempotency_cache", idempotencyConfig)
                .withCacheConfiguration("resume_cache", resumeCacheConfig)
                .withCacheConfiguration("search_cache", searchResultsCacheConfig).build();
    }
}

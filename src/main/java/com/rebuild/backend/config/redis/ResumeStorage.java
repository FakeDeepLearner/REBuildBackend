package com.rebuild.backend.config.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
public class ResumeStorage {

    private final RedisConnectionFactory redisConnectionFactory;

    private final ObjectMapper objectMapper;

    @Autowired
    public ResumeStorage(RedisConnectionFactory redisConnectionFactory,
                         @Qualifier("mapper") ObjectMapper objectMapper) {
        this.redisConnectionFactory = redisConnectionFactory;
        this.objectMapper = objectMapper;
    }

    @Bean
    public RedisCacheManager resumeCacheManager(){
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig().
                entryTtl(Duration.ofSeconds(30)).
                serializeValuesWith(RedisSerializationContext.
                        SerializationPair.
                        fromSerializer(new RedisResumeSerializer(objectMapper)));

        return RedisCacheManager.builder(RedisCacheWriter.lockingRedisCacheWriter(redisConnectionFactory)).
                cacheDefaults(config).
                withCacheConfiguration("resume_cache", config).build();
    }
}

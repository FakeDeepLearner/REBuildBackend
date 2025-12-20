package com.rebuild.backend.config.redis;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
public class RedisCachesConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    private final RedisResumeSerializer resumeSerializer;

    @Autowired
    public RedisCachesConfig(RedisResumeSerializer resumeSerializer) {
        this.resumeSerializer = resumeSerializer;
    }

    @Bean
    public LettuceConnectionFactory connectionFactory(){
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration(host, port));
    }

    @Bean
    public RedisCacheManager cacheManager(LettuceConnectionFactory connectionFactory) {
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

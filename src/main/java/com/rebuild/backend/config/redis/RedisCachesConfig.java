package com.rebuild.backend.config.redis;

import com.rebuild.backend.model.entities.resume_entities.Resume;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
public class RedisCachesConfig {

    @Bean
    public LettuceConnectionFactory connectionFactory(){
        RedisStandaloneConfiguration standaloneConfiguration =
                new RedisStandaloneConfiguration(System.getenv("REDIS_DATABASE_URL"),
                        Integer.parseInt(System.getenv("REDIS_DATABASE_PORT")));

        standaloneConfiguration.setPassword(System.getenv("REDIS_DATABASE_PASSWORD"));

        LettuceClientConfiguration clientConfiguration = LettuceClientConfiguration.builder().
                commandTimeout(Duration.ofSeconds(10))
                .shutdownTimeout(Duration.ofMinutes(1))
                .useSsl().build();

        return new LettuceConnectionFactory(standaloneConfiguration, clientConfiguration);
    }

    @Bean
    public RedisCacheManager cacheManager(LettuceConnectionFactory connectionFactory) {
        RedisCacheConfiguration resumeCacheConfig = RedisCacheConfiguration.defaultCacheConfig().
                entryTtl(Duration.ofSeconds(30)).
                serializeValuesWith(RedisSerializationContext.
                        SerializationPair.
                        fromSerializer(new JacksonJsonRedisSerializer<>(Resume.class)));

        RedisCacheConfiguration idempotencyConfig = RedisCacheConfiguration.defaultCacheConfig().
                entryTtl(Duration.ofSeconds(30));

        return RedisCacheManager.builder(connectionFactory)
                .withCacheConfiguration("idempotency_cache", idempotencyConfig)
                .withCacheConfiguration("resume_cache", resumeCacheConfig)
                .build();
    }
}

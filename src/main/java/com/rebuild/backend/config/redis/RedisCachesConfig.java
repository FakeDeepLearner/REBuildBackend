package com.rebuild.backend.config.redis;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.cache.autoconfigure.CacheProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
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

    private final RedisProfileSerializer profileSerializer;

    private final Dotenv dotenv;

    @Autowired
    public RedisCachesConfig(RedisResumeSerializer resumeSerializer,
                             RedisProfileSerializer profileSerializer) {
        this.resumeSerializer = resumeSerializer;
        this.profileSerializer = profileSerializer;
        this.dotenv = dotenv;
    }

    @Bean
    public LettuceConnectionFactory connectionFactory(){
        RedisStandaloneConfiguration standaloneConfiguration =
                new RedisStandaloneConfiguration(dotenv.get("REDIS_DATABASE_URL"),
                        Integer.parseInt(dotenv.get("REDIS_DATABASE_PORT")));

        standaloneConfiguration.setPassword(dotenv.get("REDIS_DATABASE_PASSWORD"));

        LettuceClientConfiguration clientConfiguration = LettuceClientConfiguration.builder().
                commandTimeout(Duration.ofSeconds(10))
                .shutdownTimeout(Duration.ofMinutes(1))
                .useSsl().build();

        return new LettuceConnectionFactory(standaloneConfiguration, clientConfiguration);
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

        RedisCacheConfiguration profilesCacheConfig = RedisCacheConfiguration.defaultCacheConfig().
                entryTtl(Duration.ofMinutes(1)).
                serializeValuesWith(RedisSerializationContext.
                        SerializationPair.fromSerializer(profileSerializer));

        RedisCacheConfiguration idempotencyConfig = RedisCacheConfiguration.defaultCacheConfig().
                entryTtl(Duration.ofSeconds(30));


        return RedisCacheManager.builder(RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory))
                .withCacheConfiguration("idempotency_cache", idempotencyConfig)
                .withCacheConfiguration("resume_cache", resumeCacheConfig)
                .withCacheConfiguration("search_cache", searchResultsCacheConfig)
                .withCacheConfiguration("profile_cache", profilesCacheConfig)
                .build();
    }
}

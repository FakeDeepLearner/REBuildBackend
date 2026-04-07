package com.rebuild.backend.config.redis;

import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ResourceElementResolver;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson3JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisCachesConfig {

    private final Dotenv dotenv;

    @Autowired
    public RedisCachesConfig(Dotenv dotenv) {
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
        RedisCacheConfiguration resumeCacheConfig = RedisCacheConfiguration.defaultCacheConfig().
                entryTtl(Duration.ofSeconds(30)).
                serializeValuesWith(RedisSerializationContext.
                        SerializationPair.
                        fromSerializer(new Jackson3JsonRedisSerializer<>(Resume.class)));

        RedisCacheConfiguration idempotencyConfig = RedisCacheConfiguration.defaultCacheConfig().
                entryTtl(Duration.ofSeconds(30));

        return RedisCacheManager.builder(connectionFactory)
                .withCacheConfiguration("idempotency_cache", idempotencyConfig)
                .withCacheConfiguration("resume_cache", resumeCacheConfig)
                .build();
    }
}

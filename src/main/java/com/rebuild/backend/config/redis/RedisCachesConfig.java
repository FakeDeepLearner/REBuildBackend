package com.rebuild.backend.config.redis;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import java.time.Duration;

@Configuration
public class RedisCachesConfig {

    @Bean
    public ProxyManager<String> bucketProxyManager(){

        RedisURI uri = RedisURI.Builder.redis(System.getenv("REDIS_DATABASE_URL"),
                        Integer.parseInt(System.getenv("REDIS_DATABASE_PORT"))).
                withAuthentication("default", System.getenv("REDIS_DATABASE_PASSWORD")).
                withSsl(true).
                build();

        RedisClient redisClient = RedisClient.create(uri);

        StatefulRedisConnection<String, byte[]> connection = redisClient.connect(
                RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));

        RedisAsyncCommands<String, byte[]> asyncCommands = connection.async();

        return LettuceBasedProxyManager.builderFor(asyncCommands).build();

    }

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

        RedisCacheConfiguration idempotencyConfig = RedisCacheConfiguration.defaultCacheConfig().
                entryTtl(Duration.ofSeconds(30));

        return RedisCacheManager.builder(connectionFactory)
                .withCacheConfiguration("idempotency_cache", idempotencyConfig)
                .build();
    }
}

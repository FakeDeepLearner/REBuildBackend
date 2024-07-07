package com.rebuild.backend.config.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;

@Configuration
@EnableCaching
public class TokenStorage {

    private final RedisConnectionFactory connectionFactory;

    @Autowired
    public TokenStorage(RedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Bean
    public RedisCacheManager redisCacheManager(){
        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig().
                entryTtl(Duration.ofMinutes(15)).
                disableCachingNullValues();

        return RedisCacheManager.
                builder(RedisCacheWriter.lockingRedisCacheWriter(connectionFactory)).
                cacheDefaults(cacheConfiguration).
                withCacheConfiguration("jwt_tokens", cacheConfiguration).
                disableCreateOnMissingCache().
                transactionAware().
                build();
    }

}

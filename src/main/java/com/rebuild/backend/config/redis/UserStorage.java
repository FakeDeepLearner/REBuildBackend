package com.rebuild.backend.config.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;

@Configuration
public class UserStorage {
    private final RedisConnectionFactory connectionFactory;

    private final int hoursBlocked;

    @Autowired
    public UserStorage(RedisConnectionFactory connectionFactory,
                       @Value(value = "${spring.security.rate-limiting.user-block-hours}") int hoursBlocked) {
        this.connectionFactory = connectionFactory;
        this.hoursBlocked = hoursBlocked;
    }

    @Bean
    public RedisCacheManager userCacheManager(){
        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig().
                entryTtl(Duration.ofHours(hoursBlocked)).
                disableCachingNullValues();

        return RedisCacheManager.
                builder(RedisCacheWriter.lockingRedisCacheWriter(connectionFactory)).
                cacheDefaults(cacheConfiguration).
                withCacheConfiguration("blocked_usernames", cacheConfiguration).
                disableCreateOnMissingCache().
                transactionAware().
                build();
    }
}

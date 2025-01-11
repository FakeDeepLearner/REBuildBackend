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
public class IPStorage{

    private final RedisConnectionFactory connectionFactory;

    private final int hoursBlocked;

    @Autowired
    public IPStorage(RedisConnectionFactory connectionFactory,
                     @Value(value = "${spring.security.rate-limiting.ip-block-hours}") int hoursBlocked) {
        this.connectionFactory = connectionFactory;
        this.hoursBlocked = hoursBlocked;
    }

    @Bean
        public RedisCacheManager ipCacheManager(){
        RedisCacheConfiguration blockedConfiguration = RedisCacheConfiguration.defaultCacheConfig().
                entryTtl(Duration.ofHours(hoursBlocked)).
                disableCachingNullValues();

        RedisCacheConfiguration connectionsConfiguration = RedisCacheConfiguration.defaultCacheConfig().
                entryTtl(Duration.ofSeconds(1)).disableCachingNullValues();

        return RedisCacheManager.
                builder(RedisCacheWriter.lockingRedisCacheWriter(connectionFactory)).
                withCacheConfiguration("blocked_ips", blockedConfiguration).
                withCacheConfiguration("connection_counts", connectionsConfiguration).
                disableCreateOnMissingCache().
                transactionAware().
                build();
    }

}

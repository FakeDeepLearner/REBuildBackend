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
        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig().
                entryTtl(Duration.ofHours(hoursBlocked)).
                disableCachingNullValues();

        return RedisCacheManager.
                builder(RedisCacheWriter.lockingRedisCacheWriter(connectionFactory)).
                cacheDefaults(cacheConfiguration).
                withCacheConfiguration("blocked_ips", cacheConfiguration).
                disableCreateOnMissingCache().
                transactionAware().
                build();
    }

}

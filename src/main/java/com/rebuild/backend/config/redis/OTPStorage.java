package com.rebuild.backend.config.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;

@Configuration
public class OTPStorage {

    private final RedisConnectionFactory connectionFactory;

    @Autowired
    public OTPStorage(RedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Bean
    public RedisCacheManager otpCacheManager(){
        int minutesActive = 10;
        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig().
                entryTtl(Duration.ofMinutes(minutesActive)).
                disableCachingNullValues();

        return RedisCacheManager.builder(RedisCacheWriter.lockingRedisCacheWriter(connectionFactory)).
                cacheDefaults(cacheConfiguration).
                withCacheConfiguration("email_otp", cacheConfiguration).
                withCacheConfiguration("phone_otp", cacheConfiguration).
                withCacheConfiguration("reactivation_otp", cacheConfiguration).
                transactionAware().
                build();
    }


}

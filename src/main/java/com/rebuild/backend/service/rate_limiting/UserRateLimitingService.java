package com.rebuild.backend.service.rate_limiting;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;

@Service
public class UserRateLimitingService {
    private final RedisCacheManager emailsCacheManager;

    private final RedisCacheManager connectionsCacheManager;

    private final int hoursBlocked;

    private final int userRequestLimit;

    public UserRateLimitingService(@Qualifier("emailsCacheManager") RedisCacheManager emailsCacheManager,
                                   @Qualifier("connectionsCacheManager") RedisCacheManager connectionsCacheManager,
                                   @Value(value = "${spring.security.rate-limiting.user-block-hours}") int hoursBlocked,
                                   @Value("${spring.security.rate-limiting.user-request-limit}") int userRequestLimit) {
        this.emailsCacheManager = emailsCacheManager;
        this.connectionsCacheManager = connectionsCacheManager;
        this.hoursBlocked = hoursBlocked;
        this.userRequestLimit = userRequestLimit;
    }

    public void blockEmail(String email){
        Cache usernameCache = emailsCacheManager.getCache("blocked_emails");
        if (usernameCache != null) {
            //Put the email as the key and expiration time as the value
            usernameCache.putIfAbsent(email, Instant.now().plus(hoursBlocked, ChronoUnit.HOURS));
        }
    }

    public void registerConnection(String email){
        if(!isEmailBlocked(email)) {
            Cache connectionsCache = connectionsCacheManager.getCache("email_connections");
            if (connectionsCache != null) {
                Cache.ValueWrapper wrapper = connectionsCache.get(email);
                //If the email doesn't already exist, add it to the cache with a value of 1
                if (wrapper == null) {
                    connectionsCache.put(email, 1);
                    return;
                }
                Integer actualValue = (Integer) wrapper.get();
                connectionsCache.put(email, actualValue + 1);
                if (actualValue + 1 == userRequestLimit) {
                    blockEmail(email);
                }

            }
        }
    }

    public boolean isEmailBlocked(String email){
        Cache usernameCache = emailsCacheManager.getCache("blocked_emails");
        assert usernameCache != null;
        Cache.ValueWrapper addressExists = usernameCache.get(email);
        return addressExists != null;
    }

    public TemporalAmount getTimeRemaining(String email){
        Cache usernameCache = emailsCacheManager.getCache("blocked_emails");
        assert usernameCache != null;
        Cache.ValueWrapper usernameValue = usernameCache.get(email);
        assert usernameValue != null;
        Instant actualValue = (Instant) usernameValue.get();
        Instant current = Instant.now();
        return Duration.between(current, actualValue);
    }
}

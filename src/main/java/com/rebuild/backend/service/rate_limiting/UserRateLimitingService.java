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
    private final RedisCacheManager cacheManager;

    private final int hoursBlocked;

    public UserRateLimitingService(@Qualifier("userCacheManager") RedisCacheManager cacheManager,
                                   @Value(value = "${spring.security.rate-limiting.user-block-hours}") int hoursBlocked) {
        this.cacheManager = cacheManager;
        this.hoursBlocked = hoursBlocked;
    }

    public void blockUsername(String username){
        Cache usernameCache = cacheManager.getCache("blocked_usernames");
        if (usernameCache != null) {
            //Put the username as the key and expiration time as the value
            usernameCache.putIfAbsent(username, Instant.now().plus(hoursBlocked, ChronoUnit.HOURS));
        }
    }

    public boolean isUserBlocked(String username){
        Cache usernameCache = cacheManager.getCache("blocked_usernames");
        assert usernameCache != null;
        Cache.ValueWrapper addressExists = usernameCache.get(username);
        return addressExists != null;
    }

    public TemporalAmount getTimeRemaining(String username){
        Cache usernameCache = cacheManager.getCache("blocked_usernames");
        assert usernameCache != null;
        Cache.ValueWrapper usernameValue = usernameCache.get(username);
        assert usernameValue != null;
        Instant actualValue = (Instant) usernameValue.get();
        Instant current = Instant.now();
        return Duration.between(current, actualValue);
    }
}

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
public class IPRateLimitingService {

    private final RedisCacheManager cacheManager;

    private final int hoursBlocked;

    public IPRateLimitingService(@Qualifier("ipCacheManager") RedisCacheManager cacheManager,
                                 @Value(value = "${spring.security.rate-limiting.ip-block-hours}") int hoursBlocked) {
        this.cacheManager = cacheManager;
        this.hoursBlocked = hoursBlocked;
    }

    public void blockIpAddress(String address){
        Cache ipCache = cacheManager.getCache("blocked_ips");
        if (ipCache != null) {
            //Put the address as the key and expiration time as the value
            ipCache.putIfAbsent(address, Instant.now().plus(hoursBlocked, ChronoUnit.HOURS));
        }
    }

    public boolean isAddressBlocked(String address){
        Cache ipCache = cacheManager.getCache("blocked_ips");
        assert ipCache != null;
        Cache.ValueWrapper addressExists = ipCache.get(address);
        return addressExists != null;
    }

    public TemporalAmount getTimeRemaining(String address){
        Cache ipCache = cacheManager.getCache("blocked_ips");
        assert ipCache != null;
        Cache.ValueWrapper addressValue = ipCache.get(address);
        assert addressValue != null;
        Instant value = (Instant) addressValue.get();
        Instant current = Instant.now();
        return Duration.between(current, value);
    }
}

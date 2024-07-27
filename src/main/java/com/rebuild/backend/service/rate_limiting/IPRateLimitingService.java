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

    private final int ipRequestLimit;

    public IPRateLimitingService(@Qualifier("ipCacheManager") RedisCacheManager cacheManager,
                                 @Value(value = "${spring.security.rate-limiting.ip-block-hours}") int hoursBlocked,
                                 @Value("${spring.security.rate-limiting.ip-request-limit}") int ipRequestLimit) {
        this.cacheManager = cacheManager;
        this.hoursBlocked = hoursBlocked;
        this.ipRequestLimit = ipRequestLimit;
    }

    public void blockIpAddress(String address){
        Cache ipCache = cacheManager.getCache("blocked_ips");
        if (ipCache != null) {
            //Put the address as the key and expiration time as the value
            ipCache.putIfAbsent(address, Instant.now().plus(hoursBlocked, ChronoUnit.HOURS));
        }
    }

    public void registerConnection(String address){
        if(!isAddressBlocked(address)) {
            Cache connectionsCache = cacheManager.getCache("connection_counts");
            if (connectionsCache != null) {
                Cache.ValueWrapper wrapper = connectionsCache.get(address);
                //If the address doesn't already exist, add it to the cache with a value of 1
                if (wrapper == null) {
                    connectionsCache.put(address, 1);
                    return;
                }
                Integer actualValue = (Integer) wrapper.get();
                connectionsCache.put(address, actualValue + 1);
                if (actualValue + 1 == ipRequestLimit) {
                    blockIpAddress(address);
                }

            }
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

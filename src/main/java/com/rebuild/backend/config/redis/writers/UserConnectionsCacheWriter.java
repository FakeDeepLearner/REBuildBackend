package com.rebuild.backend.config.redis.writers;

import com.rebuild.backend.service.user_services.UserService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.cache.CacheStatistics;
import org.springframework.data.redis.cache.CacheStatisticsCollector;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

@Component
public class UserConnectionsCacheWriter implements RedisCacheWriter {

    private final RedisCacheWriter writer;

    private final UserService userService;

    @Autowired
    public UserConnectionsCacheWriter(RedisConnectionFactory connectionFactory, UserService userService) {
        this.writer = RedisCacheWriter.lockingRedisCacheWriter(connectionFactory);
        this.userService = userService;
    }

    @Override
    public void put(@NonNull String name, byte @NonNull [] key, byte @NonNull [] value, Duration ttl) {
        writer.put(name, key, value, ttl);
        userService.lockUserAccount(Arrays.toString(key));
    }

    @Override
    public @NonNull CompletableFuture<Void> store(@NonNull String name, byte @NonNull [] key,
                                                  byte @NonNull [] value, Duration ttl) {
        return writer.store(name, key, value, ttl);
    }

    @Override
    public byte[] get(@NonNull String name, byte @NonNull [] key) {
        return writer.get(name, key);
    }

    @Override
    public @NonNull CompletableFuture<byte[]> retrieve(@NonNull String name,
                                                       byte @NonNull [] key, Duration ttl) {
        return writer.retrieve(name, key, ttl);
    }

    @Override
    public byte[] putIfAbsent(@NonNull String name, byte @NonNull [] key, byte @NonNull [] value, Duration ttl) {
        return writer.putIfAbsent(name, key, value, ttl);
    }

    @Override
    public void remove(@NonNull String name, byte @NonNull [] key) {
        writer.remove(name, key);
        userService.unlockUserAccount(Arrays.toString(key));
    }

    @Override
    public void clean(@NonNull String name, byte @NonNull [] pattern) {
        writer.clean(name, pattern);
    }

    @Override
    public void clearStatistics(@NonNull String name) {
        writer.clearStatistics(name);
    }

    @Override
    public @NonNull RedisCacheWriter withStatisticsCollector(@NonNull CacheStatisticsCollector cacheStatisticsCollector) {
       return writer.withStatisticsCollector(cacheStatisticsCollector);
    }

    @Override
    public @NonNull CacheStatistics getCacheStatistics(@NonNull String cacheName) {
        return writer.getCacheStatistics(cacheName);
    }
}

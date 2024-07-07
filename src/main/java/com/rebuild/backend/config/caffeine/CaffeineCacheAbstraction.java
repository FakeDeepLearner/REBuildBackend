package com.rebuild.backend.config.caffeine;

import lombok.NonNull;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import java.util.concurrent.Callable;


public class CaffeineCacheAbstraction<K, V> implements Cache {

    private final String name;

    private final com.github.benmanes.caffeine.cache.Cache<K, V> caffeineCache;

    public CaffeineCacheAbstraction(String name,
                                    com.github.benmanes.caffeine.cache.Cache<K, V> caffeineCache) {
        this.name = name;
        this.caffeineCache = caffeineCache;
    }

    @Override
    public @NonNull String getName() {
        return name;
    }

    @Override
    public @NonNull Object getNativeCache() {
        return caffeineCache;
    }

    @Override
    public ValueWrapper get(@NonNull Object key) {
        Object value = caffeineCache.getIfPresent((K) key);
        return (value != null ? new SimpleValueWrapper(value) : null);
    }

    @Override
    public <T> T get(@NonNull Object key, Class<T> type) {
        V value = caffeineCache.getIfPresent((K) key);
        return (type != null && type.isInstance(value) ? type.cast(value) : null);
    }

    @Override
    public <T> T get(@NonNull Object key, @NonNull Callable<T> valueLoader) {
        Object value = caffeineCache.getIfPresent((K) key);

        if (value == null){
            try {
                return valueLoader.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return (T) value;
    }

    @Override
    public void put(@NonNull Object key, Object value) {
        caffeineCache.put((K) key, (V) value);
    }

    @Override
    public void evict(Object key) {
        caffeineCache.invalidate((K) key);
    }

    @Override
    public void clear() {
        caffeineCache.invalidateAll();
    }
}

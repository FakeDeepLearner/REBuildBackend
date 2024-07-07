package com.rebuild.backend.config.caffeine;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



import java.util.Collections;
import java.util.UUID;

@Configuration
public class CaffeineCacheManagerConfig{

    private final LoadingCache<UUID, Resume> resumeLoadingCache;

    @Autowired
    public CaffeineCacheManagerConfig(LoadingCache<UUID, Resume> resumeLoadingCache) {
        this.resumeLoadingCache = resumeLoadingCache;
    }


    @Bean
    public CacheManager caffeineCacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        Cache resumeCacheWrapper = new CaffeineCacheAbstraction<>("resume_cache", resumeLoadingCache);
        manager.setCaches(Collections.singletonList(resumeCacheWrapper));

        return manager;
    }
}

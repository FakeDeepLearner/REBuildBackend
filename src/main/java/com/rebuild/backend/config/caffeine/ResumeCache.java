package com.rebuild.backend.config.caffeine;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Configuration
public class ResumeCache {

    private final ResumeService resumeService;

    @Autowired
    public ResumeCache(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @Bean
    LoadingCache<UUID, Resume> resumeLoadingCache(){

        return Caffeine.newBuilder().
                expireAfterAccess(2, TimeUnit.MINUTES).
                expireAfterWrite(10, TimeUnit.MINUTES).
                maximumSize(1000).
                build(resumeService::findById);
    }


}

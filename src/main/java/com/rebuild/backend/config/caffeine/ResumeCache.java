package com.rebuild.backend.config.caffeine;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.service.resume_services.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
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
                expireAfterAccess(1, TimeUnit.MINUTES).
                expireAfterWrite(15, TimeUnit.SECONDS).
                maximumSize(100).
                build(resumeService::findById);
    }


}

package com.rebuild.backend.utils;

import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.repository.ResumeRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class ResumeGetUtility {

    private final ResumeRepository resumeRepository;

    private final CacheManager cacheManager;

    private final Cache resumeCache;

    public ResumeGetUtility(ResumeRepository resumeRepository,
                            @Qualifier("caffeineCacheManager") CacheManager cacheManager) {
        this.resumeRepository = resumeRepository;
        this.cacheManager = cacheManager;
        this.resumeCache = cacheManager.getCache("resume_cache");
    }

    public Resume getResume(UUID resumeId){
        Resume foundResumeInCache = resumeCache.get(resumeId, Resume.class);
        if(foundResumeInCache == null){
            return resumeRepository.findById(resumeId).orElseThrow();
        }
        return foundResumeInCache;
    }
}

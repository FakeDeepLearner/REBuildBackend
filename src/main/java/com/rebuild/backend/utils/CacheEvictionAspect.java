package com.rebuild.backend.utils;

import com.rebuild.backend.model.entities.resume_entities.Resume;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Aspect
public class CacheEvictionAspect {

    private final RedisCacheManager cacheManager;

    @Autowired
    public CacheEvictionAspect(RedisCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Pointcut("within(com.rebuild.backend.service.resume_services.ResumeService) " +
            "&& @annotation(org.springframework.transaction.annotation.Transactional)")
    public void evictionPointcut() {}

    public void evictFromCache(UUID resumeId){
        Cache resumeCache = cacheManager.getCache("resume_cache");
        assert resumeCache != null;
        resumeCache.evict(resumeId);
    }


    @AfterReturning(pointcut = "evictionPointcut()", returning = "result")
    public void triggerCacheEviction(JoinPoint joinPoint, Object result) {
        if(result == null) {
            return;
        }

        if(result instanceof Resume resume) {
            evictFromCache(resume.getId());
        }
    }
}

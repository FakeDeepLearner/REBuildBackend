package com.rebuild.backend.utils;

import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.repository.ResumeRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ResumeGetUtility {

    private final ResumeRepository resumeRepository;

    private final Cache resumeCache;

    public ResumeGetUtility(ResumeRepository resumeRepository,
                            @Qualifier("resumeCacheManager") RedisCacheManager cacheManager) {
        this.resumeRepository = resumeRepository;
        this.resumeCache = cacheManager.getCache("resume_cache");
    }

    @Cacheable(value = "resume_cache", key = "#resumeId")
    public Resume findById(UUID resumeId){
        return resumeRepository.findById(resumeId).orElse(null);
    }

    @Cacheable(value = "resume_cache", key = "#searchingUser.id.toString()" + "-" + "#index")
    public Resume findByUserResumeIndex(User searchingUser, int index){
        List<Resume> userResumes = searchingUser.getResumes();

        return userResumes.get(index);
    }


}

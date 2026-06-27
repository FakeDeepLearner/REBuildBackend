package com.rebuild.backend.service.resume_services;

import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.utils.exceptions.BelongingException;
import com.rebuild.backend.repository.resume_repositories.ResumeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@Transactional(readOnly = true)
public class ResumeObtainer {
    private final ResumeRepository resumeRepository;

    @Autowired
    public ResumeObtainer(ResumeRepository resumeRepository) {
        this.resumeRepository = resumeRepository;
    }

    public Resume findByUserResumeId(User searchingUser, UUID resumeId){
        return resumeRepository.findByIdAndUser(resumeId, searchingUser).orElseThrow(
                () -> new BelongingException("Resume either does not exist or does not belong to you.")
        );
    }

    public Resume findByUserAndIdWithAllInfo(User searchingUser, UUID resumeId){
        return resumeRepository.findByIdAndUserWithOtherData(resumeId, searchingUser).orElseThrow(
                () -> new BelongingException("Resume either does not exist or does not belong to you.")
        );
    }

    public Resume findByUserAndIdWithHeader(User searchingUser, UUID resumeId){
        return resumeRepository.findByIdAndUserWithHeader(resumeId, searchingUser).orElseThrow(
            () -> new BelongingException("Resume either does not exist or does not belong to you.")
        );
    }

    public Resume findByUserAndIdWithEducation(User searchingUser, UUID resumeId){
        return resumeRepository.findByIdAndUserWithEducation(resumeId, searchingUser).orElseThrow(
                () -> new BelongingException("Resume either does not exist or does not belong to you.")
        );
    }

    public Resume findByUserAndIdWithExperiences(User searchingUser, UUID resumeId){
        return resumeRepository.findByIdAndUserWithExperiences(resumeId, searchingUser).orElseThrow(
                () -> new BelongingException("Resume either does not exist or does not belong to you.")
        );
    }

    public Resume findByUserAndIdWithProjects(User searchingUser, UUID resumeId){
        return resumeRepository.findByIdAndUserWithProjects(resumeId, searchingUser).orElseThrow(
                () -> new BelongingException("Resume either does not exist or does not belong to you.")
        );
    }

}

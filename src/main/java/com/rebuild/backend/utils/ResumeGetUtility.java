package com.rebuild.backend.utils;

import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.exceptions.BelongingException;
import com.rebuild.backend.repository.resume_repositories.ResumeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ResumeGetUtility {

    private final ResumeRepository resumeRepository;

    @Autowired
    public ResumeGetUtility(ResumeRepository resumeRepository) {
        this.resumeRepository = resumeRepository;
    }

    @Cacheable(value = "resume_cache", key = "#searchingUser.id.toString() + ':' + #resumeId.toString()")
    public Resume findByUserResumeId(User searchingUser, UUID resumeId){
        return resumeRepository.findByIdAndUser(resumeId, searchingUser).orElseThrow(
                () -> new BelongingException("Resume either does not exist or does not belong to you.")
        );
    }


}

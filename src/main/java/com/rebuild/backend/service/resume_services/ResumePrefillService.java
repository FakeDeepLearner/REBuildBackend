package com.rebuild.backend.service.resume_services;

import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.responses.resume_responses.*;
import com.rebuild.backend.repository.resume_repositories.ResumeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ResumePrefillService {

    private final ResumeObtainer resumeObtainer;


    private final ResumeRepository resumeRepository;

    @Autowired
    public ResumePrefillService(ResumeObtainer resumeObtainer, ResumeRepository resumeRepository) {
        this.resumeObtainer = resumeObtainer;
        this.resumeRepository = resumeRepository;
    }

    
    public ResumeResponse prefillResumeHeader(UUID currentResumeId, UUID sampleResumeId, User user)
    {
        Resume currentResume = resumeObtainer.findByUserResumeId(user, currentResumeId);

        Resume sampleResume = resumeObtainer.findByUserAndIdWithHeader(user, sampleResumeId);

        ResumeHeader newResumeHeader = ResumeHeader.copy(sampleResume.getResumeHeader());

        newResumeHeader.setResume(currentResume);
        currentResume.setResumeHeader(newResumeHeader);

        Resume savedResume = resumeRepository.save(currentResume);

        return savedResume.toResponse();
    }

    
    public ResumeResponse prefillResumeEducation(UUID currentResumeId, UUID sampleResumeId, User user)
    {
        Resume currentResume = resumeObtainer.findByUserResumeId(user, currentResumeId);

        Resume sampleResume = resumeObtainer.findByUserAndIdWithEducation(user, sampleResumeId);

        ResumeEducation newResumeEducation = ResumeEducation.copy(sampleResume.getResumeEducation());

        newResumeEducation.setResume(currentResume);
        currentResume.setResumeEducation(newResumeEducation);

        Resume savedResume = resumeRepository.save(currentResume);

        return savedResume.toResponse();
    }

    
    public ResumeResponse prefillResumeExperiences(UUID currentResumeId, UUID sampleResumeId, User user)
    {
        Resume currentResume = resumeObtainer.findByUserResumeId(user, currentResumeId);

        Resume sampleResume = resumeObtainer.findByUserAndIdWithExperiences(user, sampleResumeId);

        Set<ResumeExperience> newResumeExperiences = sampleResume.getResumeExperiences().stream()
                .map(resumeExperience -> new ResumeExperience(resumeExperience, currentResume)).
                collect(Collectors.toSet());

        currentResume.setResumeExperiences(newResumeExperiences);

        Resume savedResume = resumeRepository.save(currentResume);

        return savedResume.toResponse();
    }

    
    public ResumeResponse prefillResumeProjects(UUID currentResumeId, UUID sampleResumeId, User user)
    {
        Resume currentResume = resumeObtainer.findByUserResumeId(user, currentResumeId);

        Resume sampleResume = resumeObtainer.findByUserAndIdWithProjects(user, sampleResumeId);

        Set<ResumeProject> newResumeProjects = sampleResume.getResumeProjects().stream()
                .map(ResumeProject::new).
                peek(project -> project.setResume(currentResume))
                .collect(Collectors.toSet());

        currentResume.setResumeProjects(newResumeProjects);

        Resume savedResume = resumeRepository.save(currentResume);

        return savedResume.toResponse();
    }

}

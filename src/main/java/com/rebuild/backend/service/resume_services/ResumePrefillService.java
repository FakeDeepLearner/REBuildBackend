package com.rebuild.backend.service.resume_services;

import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.responses.resume_responses.EducationResponse;
import com.rebuild.backend.model.responses.resume_responses.ExperienceResponse;
import com.rebuild.backend.model.responses.resume_responses.HeaderResponse;
import com.rebuild.backend.model.responses.resume_responses.ProjectResponse;
import com.rebuild.backend.repository.resume_repositories.ResumeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ResumePrefillService {

    private final ResumeObtainer resumeObtainer;


    private final ResumeRepository resumeRepository;

    @Autowired
    public ResumePrefillService(ResumeObtainer resumeObtainer, ResumeRepository resumeRepository) {
        this.resumeObtainer = resumeObtainer;
        this.resumeRepository = resumeRepository;
    }

    @Transactional
    public HeaderResponse prefillResumeHeader(UUID currentResumeId, UUID sampleResumeId, User user)
    {
        Resume currentResume = resumeObtainer.findByUserResumeId(user, currentResumeId);

        Resume sampleResume = resumeObtainer.findByUserAndIdWithHeader(user, sampleResumeId);

        Header newHeader = Header.copy(sampleResume.getHeader());

        newHeader.setResume(currentResume);
        currentResume.setHeader(newHeader);

        resumeRepository.save(currentResume);

        return newHeader.toResponse();
    }

    @Transactional
    public EducationResponse prefillResumeEducation(UUID currentResumeId, UUID sampleResumeId, User user)
    {
        Resume currentResume = resumeObtainer.findByUserResumeId(user, currentResumeId);

        Resume sampleResume = resumeObtainer.findByUserAndIdWithEducation(user, sampleResumeId);

        Education newEducation = Education.copy(sampleResume.getEducation());

        newEducation.setResume(currentResume);
        currentResume.setEducation(newEducation);

        resumeRepository.save(currentResume);

        return newEducation.toResponse();
    }

    @Transactional
    public List<ExperienceResponse> prefillResumeExperiences(UUID currentResumeId, UUID sampleResumeId, User user)
    {
        Resume currentResume = resumeObtainer.findByUserResumeId(user, currentResumeId);

        Resume sampleResume = resumeObtainer.findByUserAndIdWithExperiences(user, sampleResumeId);

        List<Experience> newExperiences = sampleResume.getExperiences().stream()
                .map(Experience::copy).
                peek(experience -> experience.setResume(currentResume))
                .toList();

        currentResume.setExperiences(newExperiences);

        Resume savedResume = resumeRepository.save(currentResume);

        return savedResume.getExperiences().stream()
                .map(Experience::toResponse).toList();
    }

    @Transactional
    public List<ProjectResponse> prefillResumeProjects(UUID currentResumeId, UUID sampleResumeId, User user)
    {
        Resume currentResume = resumeObtainer.findByUserResumeId(user, currentResumeId);

        Resume sampleResume = resumeObtainer.findByUserAndIdWithProjects(user, sampleResumeId);

        List<Project> newProjects = sampleResume.getProjects().stream()
                .map(Project::copy).
                peek(project -> project.setResume(currentResume))
                .toList();

        currentResume.setProjects(newProjects);

        Resume savedResume = resumeRepository.save(currentResume);

        return savedResume.getProjects().stream()
                .map(Project::toResponse).toList();
    }

}

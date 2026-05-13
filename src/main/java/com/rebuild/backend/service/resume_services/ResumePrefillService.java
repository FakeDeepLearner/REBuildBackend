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

        ResumeHeader newResumeHeader = ResumeHeader.copy(sampleResume.getResumeHeader());

        newResumeHeader.setResume(currentResume);
        currentResume.setResumeHeader(newResumeHeader);

        resumeRepository.save(currentResume);

        return newResumeHeader.toResponse();
    }

    @Transactional
    public EducationResponse prefillResumeEducation(UUID currentResumeId, UUID sampleResumeId, User user)
    {
        Resume currentResume = resumeObtainer.findByUserResumeId(user, currentResumeId);

        Resume sampleResume = resumeObtainer.findByUserAndIdWithEducation(user, sampleResumeId);

        ResumeEducation newResumeEducation = ResumeEducation.copy(sampleResume.getResumeEducation());

        newResumeEducation.setResume(currentResume);
        currentResume.setResumeEducation(newResumeEducation);

        resumeRepository.save(currentResume);

        return newResumeEducation.toResponse();
    }

    @Transactional
    public List<ExperienceResponse> prefillResumeExperiences(UUID currentResumeId, UUID sampleResumeId, User user)
    {
        Resume currentResume = resumeObtainer.findByUserResumeId(user, currentResumeId);

        Resume sampleResume = resumeObtainer.findByUserAndIdWithExperiences(user, sampleResumeId);

        List<ResumeExperience> newResumeExperiences = sampleResume.getResumeExperiences().stream()
                .map(ResumeExperience::copy).
                peek(experience -> experience.setResume(currentResume))
                .toList();

        currentResume.setResumeExperiences(newResumeExperiences);

        Resume savedResume = resumeRepository.save(currentResume);

        return savedResume.getResumeExperiences().stream()
                .map(ResumeExperience::toResponse).toList();
    }

    @Transactional
    public List<ProjectResponse> prefillResumeProjects(UUID currentResumeId, UUID sampleResumeId, User user)
    {
        Resume currentResume = resumeObtainer.findByUserResumeId(user, currentResumeId);

        Resume sampleResume = resumeObtainer.findByUserAndIdWithProjects(user, sampleResumeId);

        List<ResumeProject> newResumeProjects = sampleResume.getResumeProjects().stream()
                .map(ResumeProject::copy).
                peek(project -> project.setResume(currentResume))
                .toList();

        currentResume.setResumeProjects(newResumeProjects);

        Resume savedResume = resumeRepository.save(currentResume);

        return savedResume.getResumeProjects().stream()
                .map(ResumeProject::toResponse).toList();
    }

}

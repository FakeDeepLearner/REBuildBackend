package com.rebuild.backend.service.resume_services;

import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.exceptions.PrefillException;
import com.rebuild.backend.model.forms.resume_forms.ExperienceForm;
import com.rebuild.backend.model.forms.resume_forms.ProjectForm;
import com.rebuild.backend.repository.resume_repositories.ResumeRepository;
import com.rebuild.backend.repository.user_repositories.ProfileRepository;
import com.rebuild.backend.utils.ResumeObtainer;
import com.rebuild.backend.utils.converters.YearMonthStringOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ResumePrefillService {

    private final ResumeObtainer getUtility;

    private final ProfileRepository profileRepository;

    private final ResumeRepository resumeRepository;

    @Autowired
    public ResumePrefillService(ResumeObtainer getUtility, ProfileRepository profileRepository,
                                ResumeRepository resumeRepository) {
        this.getUtility = getUtility;
        this.profileRepository = profileRepository;
        this.resumeRepository = resumeRepository;
    }

    public Resume prefillHeader(UUID resumeID, User authenticatedUser)
    {
        Resume associatedResume = getUtility.findByUserResumeId(authenticatedUser, resumeID);
        UserProfile profile = profileRepository.findByUserWithHeader(authenticatedUser);
        Header header = profile.getHeader();
        if(header == null){
            throw new PrefillException("Your profile does not have a header set");
        }
        Header newHeader = Header.copy(header);
        associatedResume.setHeader(newHeader);
        return resumeRepository.save(associatedResume);
    }

    public Resume prefillEducation(UUID resumeID, User authenticatedUser)
    {
        Resume associatedResume = getUtility.findByUserResumeId(authenticatedUser, resumeID);
        UserProfile profile = profileRepository.findByUserWithEducation(authenticatedUser);
        Education education = profile.getEducation();
        if(education == null){
            throw new PrefillException("Your profile does not have an education set");
        }
        Education newEducation = Education.copy(education);
        associatedResume.setEducation(newEducation);
        return resumeRepository.save(associatedResume);
    }


    public Resume prefillExperiencesList(UUID resumeID, User authenticatedUser) {

        Resume associatedResume = getUtility.findByUserResumeId(authenticatedUser, resumeID);
        UserProfile profile = profileRepository.findByUserWithExperiences(authenticatedUser);
        List<Experience> experienceList = profile.getExperienceList();
        if (experienceList == null) {
            throw new PrefillException("Your profile does not have experiences set");
        }
        List<Experience> newExperiences = experienceList.
                stream().map(Experience::copy).peek(experience -> {
                    experience.setResume(associatedResume);
                }).
                toList();
        associatedResume.setExperiences(newExperiences);
        return resumeRepository.save(associatedResume);
    }

    public Resume prefillProjectsList(UUID resumeID, User authenticatedUser) {

        Resume associatedResume = getUtility.findByUserResumeId(authenticatedUser, resumeID);
        UserProfile profile = profileRepository.findByUserWithProjects(authenticatedUser);
        List<Project> projectList = profile.getProjectList();
        if (projectList == null) {
            throw new PrefillException("Your profile does not have projects set");
        }
        List<Project> newProjects = projectList.
                stream().map(Project::copy).peek(project -> {
                    project.setResume(associatedResume);
                }).
                toList();
        associatedResume.setProjects(newProjects);
        return resumeRepository.save(associatedResume);
    }

}

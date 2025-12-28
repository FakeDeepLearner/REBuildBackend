package com.rebuild.backend.service.util_services;

import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.exceptions.BelongingException;
import com.rebuild.backend.model.forms.resume_forms.EducationForm;
import com.rebuild.backend.model.forms.resume_forms.ExperienceForm;
import com.rebuild.backend.model.forms.resume_forms.HeaderForm;
import com.rebuild.backend.repository.resume_repositories.EducationRepository;
import com.rebuild.backend.repository.resume_repositories.ExperienceRepository;
import com.rebuild.backend.repository.resume_repositories.HeaderRepository;
import com.rebuild.backend.repository.resume_repositories.ResumeRepository;
import com.rebuild.backend.repository.user_repositories.ProfileRepository;
import com.rebuild.backend.utils.ResumeObtainer;
import com.rebuild.backend.utils.converters.YearMonthStringOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
public class SubpartsModificationUtility {

    private final ProfileRepository profileRepository;

    private final ExperienceRepository experienceRepository;

    private final ResumeObtainer getUtility;

    private final ResumeRepository resumeRepository;

    @Autowired
    public SubpartsModificationUtility(ProfileRepository profileRepository,
                                       ExperienceRepository experienceRepository,
                                       ResumeObtainer getUtility, ResumeRepository resumeRepository) {
        this.profileRepository = profileRepository;
        this.experienceRepository = experienceRepository;
        this.getUtility = getUtility;
        this.resumeRepository = resumeRepository;
    }

    @Transactional
    public Resume modifyResumeHeader(HeaderForm headerForm,
                                    UUID resumeId, User changingUser)
    {
        Resume changingResume = getUtility.findByUserAndIdWithExtraInfo(changingUser, resumeId);

        Header newHeader = new Header(headerForm.number(), headerForm.firstName(), headerForm.lastName(),
                headerForm.email());

        changingResume.setHeader(newHeader);
        newHeader.setResume(changingResume);
        return resumeRepository.save(changingResume);
    }

    @Transactional
    public Resume modifyResumeExperience(ExperienceForm experienceForm, UUID experienceId,
                                    UUID resumeId, User changingUser)
    {
        Resume changingResume = getUtility.findByUserAndIdWithExtraInfo(changingUser, resumeId);
        Optional<Experience> changingExperience = changingResume.getExperiences().stream().
                filter(experience -> experience.getId() != experienceId).findFirst();
        
        if (changingExperience.isEmpty()) {
            throw new BelongingException("Experience with this id either does not exist or does not belong to this resume");
        }

        modifyExperience(changingExperience.get(), experienceForm);
        return resumeRepository.save(changingResume);
    }

    @Transactional
    public Resume modifyResumeEducation(EducationForm educationForm,
                                    UUID resumeId, User changingUser)
    {
        Resume changingResume = getUtility.findByUserAndIdWithExtraInfo(changingUser, resumeId);

        Education newEducation = new Education(educationForm.schoolName(), educationForm.relevantCoursework(),
                educationForm.location(), YearMonthStringOperations.getYearMonth(educationForm.startDate()),
                YearMonthStringOperations.getYearMonth(educationForm.endDate()));
        changingResume.setEducation(newEducation);
        newEducation.setResume(changingResume);

        return resumeRepository.save(changingResume);
    }

    @Transactional
    public UserProfile modifyProfileHeader(HeaderForm headerForm, User changingUser)
    {

        UserProfile changingProfile = profileRepository.findByUserWithAllData(changingUser);

        Header newHeader = new Header(headerForm.number(), headerForm.firstName(), headerForm.lastName(),
                headerForm.email());
        changingProfile.setHeader(newHeader);
        return profileRepository.save(changingProfile);

    }

    @Transactional
    public UserProfile modifyProfileEducation(EducationForm educationForm, User changingUser)
    {
        UserProfile changingProfile = profileRepository.findByUserWithAllData(changingUser);
        Education newEducation = new Education(educationForm.schoolName(), educationForm.relevantCoursework(),
                educationForm.location(), YearMonthStringOperations.getYearMonth(educationForm.startDate()),
                YearMonthStringOperations.getYearMonth(educationForm.endDate()));
        changingProfile.setEducation(newEducation);


        return profileRepository.save(changingProfile);
    }


    @Transactional
    public UserProfile modifyProfileExperience(ExperienceForm experienceForm, UUID experienceId, User changingUser)
    {
        UserProfile changingProfile = profileRepository.findByUserWithAllData(changingUser);
        Optional<Experience> changingExperience = changingProfile.getExperienceList().stream().
                filter(experience -> experience.getId() != experienceId).findFirst();

        if (changingExperience.isEmpty()) {
            throw new BelongingException("Experience with this id either does not exist " +
                    "or does not belong to this profile");
        }

        modifyExperience(changingExperience.get(), experienceForm);

        return profileRepository.save(changingProfile);
    }

    private void modifyExperience(Experience changingExperience, ExperienceForm experienceForm)
    {
        YearMonth start = YearMonthStringOperations.getYearMonth(experienceForm.startDate());
        YearMonth end = YearMonthStringOperations.getYearMonth(experienceForm.endDate());
        changingExperience.setLocation(experienceForm.location());
        changingExperience.setEndDate(end);
        changingExperience.setStartDate(start);
        changingExperience.setBullets(experienceForm.bullets());
        changingExperience.setTechnologyList(experienceForm.technologies());
        changingExperience.setCompanyName(experienceForm.companyName());
        changingExperience.setExperienceType(experienceForm.experienceType());
    }





}

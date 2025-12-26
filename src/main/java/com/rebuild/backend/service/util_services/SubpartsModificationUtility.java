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

    private final HeaderRepository headerRepository;

    private final RedisCacheManager cacheManager;

    private final ProfileRepository profileRepository;

    private final EducationRepository educationRepository;

    private final ExperienceRepository experienceRepository;

    private final ResumeObtainer getUtility;

    @Autowired
    public SubpartsModificationUtility(HeaderRepository headerRepository,
                                       RedisCacheManager cacheManager, ProfileRepository profileRepository,
                                       EducationRepository educationRepository,
                                       ExperienceRepository experienceRepository,
                                       ResumeObtainer getUtility) {
        this.headerRepository = headerRepository;
        this.cacheManager = cacheManager;
        this.profileRepository = profileRepository;
        this.educationRepository = educationRepository;
        this.experienceRepository = experienceRepository;
        this.getUtility = getUtility;
    }

    @Transactional
    public Header modifyResumeHeader(HeaderForm headerForm,
                                    UUID resumeId, User changingUser)
    {
        Resume changingResume = getUtility.findByUserAndIdWithExtraInfo(changingUser, resumeId);
        Header changingHeader = changingResume.getHeader();

        Header newHeader = new Header(headerForm.number(), headerForm.firstName(), headerForm.lastName(),
                headerForm.email());
        modifyHeaderData(newHeader, changingHeader);
        evictResumeFromCache(changingUser.getId(), resumeId);
        return headerRepository.save(changingHeader);
    }

    @Transactional
    public Experience modifyResumeExperience(ExperienceForm experienceForm, UUID experienceId,
                                    UUID resumeId, User changingUser)
    {
        Resume changingResume = getUtility.findByUserAndIdWithExtraInfo(changingUser, resumeId);
        Optional<Experience> changingExperience = changingResume.getExperiences().stream().
                dropWhile(experience -> experience.getId() != experienceId).findFirst();
        
        if (changingExperience.isEmpty()) {
            throw new BelongingException("Experience with this id either does not exist or does not belong to this resume");
        }

        Experience newExp = modifyExperience(changingExperience.get(), experienceForm);
        evictResumeFromCache(changingUser.getId(), resumeId);
        return newExp;
    }

    @Transactional
    public Education modifyResumeEducation(EducationForm educationForm,
                                    UUID resumeId, User changingUser)
    {
        Resume changingResume = getUtility.findByUserAndIdWithExtraInfo(changingUser, resumeId);
        Education changingEducation = changingResume.getEducation();
        Education newEducation = new Education(educationForm.schoolName(), educationForm.relevantCoursework(),
                educationForm.location(), YearMonthStringOperations.getYearMonth(educationForm.startDate()),
                YearMonthStringOperations.getYearMonth(educationForm.endDate()));

        modifyEducationData(newEducation, changingEducation);
        evictResumeFromCache(changingUser.getId(), resumeId);
        return educationRepository.save(changingEducation);
    }

    @Transactional
    public Header modifyProfileHeader(HeaderForm headerForm, User changingUser)
    {

        UserProfile changingProfile = profileRepository.findByUserWithAllData(changingUser);
        Header changingHeader = changingProfile.getHeader();

        Header newHeader = new Header(headerForm.number(), headerForm.firstName(), headerForm.lastName(),
                headerForm.email());
        modifyHeaderData(newHeader, changingHeader);
        return headerRepository.save(changingHeader);

    }

    @Transactional
    public Education modifyProfileEducation(EducationForm educationForm, User changingUser)
    {
        UserProfile changingProfile = profileRepository.findByUserWithAllData(changingUser);
        Education changingEducation = changingProfile.getEducation();
        Education newEducation = new Education(educationForm.schoolName(), educationForm.relevantCoursework(),
                educationForm.location(), YearMonthStringOperations.getYearMonth(educationForm.startDate()),
                YearMonthStringOperations.getYearMonth(educationForm.endDate()));

        modifyEducationData(newEducation, changingEducation);
        return educationRepository.save(changingEducation);
    }


    @Transactional
    public Experience modifyProfileExperience(ExperienceForm experienceForm, UUID experienceId, User changingUser)
    {
        UserProfile changingProfile = profileRepository.findByUserWithAllData(changingUser);
        Optional<Experience> changingExperience = changingProfile.getExperienceList().stream().
                dropWhile(experience -> experience.getId() != experienceId).findFirst();

        if (changingExperience.isEmpty()) {
            throw new BelongingException("Experience with this id either does not exist " +
                    "or does not belong to this profile");
        }

        return modifyExperience(changingExperience.get(), experienceForm);
    }

    private void evictResumeFromCache(UUID userId, UUID resumeId)
    {
        String combinedCacheKey = userId.toString() + ':' + resumeId.toString();
        Objects.requireNonNull(cacheManager.getCache("resume_cache")).
                evictIfPresent(combinedCacheKey);
    }

    private Experience modifyExperience(Experience changingExperience, ExperienceForm experienceForm)
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
        return experienceRepository.save(changingExperience);
    }


    public void modifyHeaderData(Header newHeader, Header currentHeader) {
        if (!currentHeader.getEmail().equals(newHeader.getEmail())){
            currentHeader.setEmail(newHeader.getEmail());
        }

        if (!currentHeader.getNumber().equals(newHeader.getNumber())){
            currentHeader.setNumber(newHeader.getNumber());
        }

        if (!currentHeader.getFirstName().equals(newHeader.getFirstName())){
            currentHeader.setFirstName(newHeader.getFirstName());
        }

        if (!currentHeader.getLastName().equals(newHeader.getLastName())){
            currentHeader.setLastName(newHeader.getLastName());
        }
    }

    public void modifyEducationData(Education newEducation, Education currentEducation) {
        if (!currentEducation.getLocation().equals(newEducation.getLocation())){
            currentEducation.setLocation(newEducation.getLocation());
        }

        if (!currentEducation.getSchoolName().equals(newEducation.getSchoolName())){
            currentEducation.setSchoolName(newEducation.getSchoolName());
        }

        if (!currentEducation.getRelevantCoursework().equals(newEducation.getRelevantCoursework())){
            currentEducation.setRelevantCoursework(newEducation.getRelevantCoursework());
        }

        if (!currentEducation.getStartDate().equals(newEducation.getStartDate())){
            currentEducation.setStartDate(newEducation.getStartDate());
        }

        // If the end date is null, this means that it is "Present", which in turn means that we don't need to do
        // any further comparisons.
        if (newEducation.getEndDate() == null || !currentEducation.getEndDate().equals(newEducation.getEndDate())){
            currentEducation.setEndDate(newEducation.getEndDate());
        }
    }




}

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
import com.rebuild.backend.utils.ResumeGetUtility;
import com.rebuild.backend.utils.YearMonthStringOperations;
import com.rebuild.backend.utils.converters.ObjectConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
public class SubpartsModificationUtility {

    private final HeaderRepository headerRepository;

    private final RedisCacheManager cacheManager;

    private final ProfileRepository profileRepository;

    private final EducationRepository educationRepository;

    private final ExperienceRepository experienceRepository;

    private final ObjectConverter objectConverter;

    private final ResumeGetUtility getUtility;

    @Autowired
    public SubpartsModificationUtility(HeaderRepository headerRepository,
                                       RedisCacheManager cacheManager, ProfileRepository profileRepository,
                                       EducationRepository educationRepository,
                                       ExperienceRepository experienceRepository,
                                       ObjectConverter objectConverter, ResumeGetUtility getUtility) {
        this.headerRepository = headerRepository;
        this.cacheManager = cacheManager;
        this.profileRepository = profileRepository;
        this.educationRepository = educationRepository;
        this.experienceRepository = experienceRepository;
        this.objectConverter = objectConverter;
        this.getUtility = getUtility;
    }

    @Transactional
    public Header modifyResumeHeader(HeaderForm headerForm, UUID headerId,
                                    UUID resumeId, User changingUser)
    {
        Resume changingResume = getUtility.findByUserResumeId(changingUser, resumeId);
        Header changingHeader = headerRepository.findByIdAndResume(headerId, changingResume).orElseThrow(
                () -> new BelongingException("The header either does not exist or does not belong to this resume.")
        );

        Header newHeader = modifyHeader(changingHeader, headerForm);
        evictResumeFromCache(changingUser.getId(), resumeId);
        return newHeader;
    }

    @Transactional
    public Experience modifyResumeExperience(ExperienceForm experienceForm, UUID experienceId,
                                    UUID resumeId, User changingUser)
    {
        Resume changingResume = getUtility.findByUserResumeId(changingUser, resumeId);
        Experience changingExperience = experienceRepository.findByIdAndResume(experienceId, changingResume).orElseThrow(
                () -> new BelongingException("The experience either does not exist or does not belong to this resume.")
        );

        Experience newExp = modifyExperience(changingExperience, experienceForm);
        evictResumeFromCache(changingUser.getId(), resumeId);
        return newExp;
    }

    @Transactional
    public Education modifyResumeEducation(EducationForm educationForm, UUID educationId,
                                    UUID resumeId, User changingUser)
    {
        Resume changingResume = getUtility.findByUserResumeId(changingUser, resumeId);
        Education changingEducation = educationRepository.findByIdAndResume(educationId, changingResume).orElseThrow(
                () -> new BelongingException("The education either does not exist or does not belong to this resume.")
        );

        Education newEducation = modifyEducation(changingEducation, educationForm);
        evictResumeFromCache(changingUser.getId(), resumeId);
        return newEducation;
    }

    @Transactional
    public Header modifyProfileHeader(HeaderForm headerForm, UUID headerId,
                                     UUID profileId, User changingUser)
    {
        UserProfile changingProfile = profileRepository.findByIdAndUser(profileId, changingUser).orElseThrow(
                () -> new BelongingException("The profile either does not exist or does not belong to you.")
        );
        Header changingHeader = headerRepository.findByIdAndProfile(headerId, changingProfile).orElseThrow(
                () -> new BelongingException("The header either does not exist or does not belong to this profile.")
        );

        return modifyHeader(changingHeader, headerForm);
    }

    @Transactional
    public Education modifyProfileEducation(EducationForm educationForm, UUID educationId,
                                     UUID profileId, User changingUser)
    {
        UserProfile changingProfile = profileRepository.findByIdAndUser(profileId, changingUser).orElseThrow(
                () -> new BelongingException("The profile either does not exist or does not belong to you.")
        );
        Education changingEducation = educationRepository.findByIdAndProfile(educationId, changingProfile).orElseThrow(
                () -> new BelongingException("The education either does not exist or does not belong to this profile.")
        );

        return modifyEducation(changingEducation, educationForm);
    }

    @Transactional
    public Experience modifyProfileExperience(ExperienceForm experienceForm, UUID experienceId,
                                     UUID profileId, User changingUser)
    {
        UserProfile changingProfile = profileRepository.findByIdAndUser(profileId, changingUser).orElseThrow(
                () -> new BelongingException("The profile either does not exist or does not belong to you.")
        );
        Experience changingExperience = experienceRepository.findByIdAndProfile(experienceId, changingProfile).orElseThrow(
                () -> new BelongingException("The experience either does not exist or does not belong to this profile.")
        );

        return modifyExperience(changingExperience, experienceForm);
    }

    private void evictResumeFromCache(UUID userId, UUID resumeId)
    {
        String combinedCacheKey = userId.toString() + ':' + resumeId.toString();
        Objects.requireNonNull(cacheManager.getCache("resume_cache")).
                evictIfPresent(combinedCacheKey);
    }

    private Header modifyHeader(Header header, HeaderForm headerForm)
    {
        header.setEmail(headerForm.email());
        header.setNumber(headerForm.number());
        header.setFirstName(headerForm.firstName());
        header.setLastName(headerForm.lastName());
        return headerRepository.save(header);
    }

    private Experience modifyExperience(Experience changingExperience, ExperienceForm experienceForm)
    {
        YearMonth start = YearMonthStringOperations.getYearMonth(experienceForm.startDate());
        YearMonth end = YearMonthStringOperations.getYearMonth(experienceForm.endDate());
        List<ExperienceType> experienceTypes = objectConverter.convertToExperienceTypes(experienceForm.experienceTypeValues());
        changingExperience.setLocation(experienceForm.location());
        changingExperience.setEndDate(end);
        changingExperience.setStartDate(start);
        changingExperience.setBullets(experienceForm.bullets());
        changingExperience.setTechnologyList(experienceForm.technologies());
        changingExperience.setCompanyName(experienceForm.companyName());
        changingExperience.setExperienceTypes(experienceTypes);
        return experienceRepository.save(changingExperience);
    }

    private Education modifyEducation(Education education, EducationForm educationForm){
        education.setRelevantCoursework(educationForm.relevantCoursework());
        education.setSchoolName(educationForm.schoolName());
        education.setLocation(educationForm.location());
        education.setStartDate(YearMonthStringOperations.getYearMonth(educationForm.startDate()));
        education.setEndDate(YearMonthStringOperations.getYearMonth(educationForm.endDate()));
        return educationRepository.save(education);
    }



}

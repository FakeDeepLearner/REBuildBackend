package com.rebuild.backend.service.util_services;

import com.rebuild.backend.model.entities.resume_entities.Education;
import com.rebuild.backend.model.entities.resume_entities.Experience;
import com.rebuild.backend.model.entities.resume_entities.ExperienceType;
import com.rebuild.backend.model.entities.resume_entities.Header;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.forms.resume_forms.EducationForm;
import com.rebuild.backend.model.forms.resume_forms.ExperienceForm;
import com.rebuild.backend.model.forms.resume_forms.HeaderForm;
import com.rebuild.backend.repository.EducationRepository;
import com.rebuild.backend.repository.ExperienceRepository;
import com.rebuild.backend.repository.HeaderRepository;
import com.rebuild.backend.utils.YearMonthStringOperations;
import com.rebuild.backend.utils.converters.ObjectConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
public class SubpartsModificationUtility {

    private final HeaderRepository headerRepository;

    private final RedisCacheManager cacheManager;


    private final EducationRepository educationRepository;

    private final ExperienceRepository experienceRepository;

    private final ObjectConverter objectConverter;

    @Autowired
    public SubpartsModificationUtility(HeaderRepository headerRepository,
                                       @Qualifier("resumeCacheManager") RedisCacheManager cacheManager,
                                       EducationRepository educationRepository,
                                       ExperienceRepository experienceRepository,
                                       ObjectConverter objectConverter) {
        this.headerRepository = headerRepository;
        this.cacheManager = cacheManager;
        this.educationRepository = educationRepository;
        this.experienceRepository = experienceRepository;
        this.objectConverter = objectConverter;
    }


    @Transactional
    public Experience modifyExperience(ExperienceForm experienceForm, UUID experienceID,
                                       User changingUser){
        Experience changingExperience = experienceRepository.findById(experienceID).orElse(null);
        assert changingExperience != null : "Experience not found";

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
        if (changingExperience.getResume() != null) {
            evictResumeFromCache(changingUser.getId(), changingExperience.getResume().getId());
        }
        return experienceRepository.save(changingExperience);

    }


    @Transactional
    public Education modifyEducation(EducationForm educationForm,
                                     UUID educationID, User changingUser){
        Education education = educationRepository.findById(educationID).orElse(null);
        assert education != null : "Education not found";
        education.setRelevantCoursework(educationForm.relevantCoursework());
        education.setSchoolName(educationForm.schoolName());
        education.setLocation(educationForm.location());
        education.setStartDate(YearMonthStringOperations.getYearMonth(educationForm.startDate()));
        education.setEndDate(YearMonthStringOperations.getYearMonth(educationForm.endDate()));
        if(education.getResume() != null){
            evictResumeFromCache(changingUser.getId(), education.getResume().getId());
        }
        return educationRepository.save(education);
    }


    @Transactional
    public Header modifyHeader(HeaderForm headerForm, UUID headerID, User changingUser){
        // undoAdder.addUndoResumeState(resID, resume);
        Header header = headerRepository.findById(headerID).orElse(null);
        assert header != null : "Header not found";
        header.setEmail(headerForm.email());
        header.setNumber(headerForm.number());
        header.setFirstName(headerForm.firstName());
        header.setLastName(headerForm.lastName());
        if(header.getResume() != null){
            evictResumeFromCache(changingUser.getId(),  header.getResume().getId());
        }
        return headerRepository.save(header);
    }

    private void evictResumeFromCache(UUID userId, UUID resumeId)
    {
        String combinedCacheKey = userId.toString() + ':' + resumeId.toString();
        Objects.requireNonNull(cacheManager.getCache("resume_cache")).
                evictIfPresent(combinedCacheKey);
    }



}

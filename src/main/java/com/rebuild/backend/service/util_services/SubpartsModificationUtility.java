package com.rebuild.backend.service.util_services;

import com.rebuild.backend.model.entities.resume_entities.Education;
import com.rebuild.backend.model.entities.resume_entities.Experience;
import com.rebuild.backend.model.entities.resume_entities.Header;
import com.rebuild.backend.model.forms.resume_forms.EducationForm;
import com.rebuild.backend.model.forms.resume_forms.ExperienceForm;
import com.rebuild.backend.model.forms.resume_forms.HeaderForm;
import com.rebuild.backend.repository.EducationRepository;
import com.rebuild.backend.repository.ExperienceRepository;
import com.rebuild.backend.repository.HeaderRepository;
import com.rebuild.backend.repository.SectionRepository;
import com.rebuild.backend.utils.YearMonthStringOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.UUID;

@Component
public class SubpartsModificationUtility {

    private final HeaderRepository headerRepository;


    private final EducationRepository educationRepository;

    private final ExperienceRepository experienceRepository;

    private final SectionRepository sectionRepository;

    @Autowired
    public SubpartsModificationUtility(HeaderRepository headerRepository, EducationRepository educationRepository, ExperienceRepository experienceRepository, SectionRepository sectionRepository) {
        this.headerRepository = headerRepository;
        this.educationRepository = educationRepository;
        this.experienceRepository = experienceRepository;
        this.sectionRepository = sectionRepository;
    }


    @Transactional
    public Experience modifyExperience(ExperienceForm experienceForm, UUID experienceID){
        Experience changingExperience = experienceRepository.findById(experienceID).orElse(null);
        assert changingExperience != null : "Experience not found";

        YearMonth start = YearMonthStringOperations.getYearMonth(experienceForm.startDate());
        YearMonth end = YearMonthStringOperations.getYearMonth(experienceForm.endDate());
        changingExperience.setLocation(experienceForm.location());
        changingExperience.setEndDate(end);
        changingExperience.setStartDate(start);
        changingExperience.setBullets(experienceForm.bullets());
        changingExperience.setTechnologyList(experienceForm.technologies());
        changingExperience.setCompanyName(experienceForm.companyName());
        return experienceRepository.save(changingExperience);

    }


    @Transactional
    public Education modifyEducation(EducationForm educationForm,
                                         UUID educationID){
        Education education = educationRepository.findById(educationID).orElse(null);
        assert education != null : "Education not found";
        education.setRelevantCoursework(educationForm.relevantCoursework());
        education.setSchoolName(educationForm.schoolName());
        education.setLocation(educationForm.location());
        education.setStartDate(YearMonthStringOperations.getYearMonth(educationForm.startDate()));
        education.setEndDate(YearMonthStringOperations.getYearMonth(educationForm.endDate()));
        return educationRepository.save(education);
    }


    @Transactional
    public Header modifyHeader(HeaderForm headerForm, UUID headerID){
        // undoAdder.addUndoResumeState(resID, resume);
        Header header = headerRepository.findById(headerID).orElse(null);
        assert header != null : "Header not found";
        header.setEmail(headerForm.email());
        header.setNumber(headerForm.number());
        header.setFirstName(headerForm.firstName());
        header.setLastName(headerForm.lastName());
        return headerRepository.save(header);
    }



}

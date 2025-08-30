package com.rebuild.backend.service.resume_services;


import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.forms.resume_forms.*;
import com.rebuild.backend.repository.*;

import com.rebuild.backend.service.util_services.SubpartsModificationUtility;
import com.rebuild.backend.utils.ResumeGetUtility;
import com.rebuild.backend.utils.YearMonthStringOperations;
import com.rebuild.backend.utils.converters.ObjectConverter;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ResumeService {

    private final ResumeRepository resumeRepository;

    private final ObjectConverter objectConverter;

    private final SubpartsModificationUtility modificationUtility;

    private final ResumeGetUtility getUtility;

    @Autowired
    public ResumeService(ResumeRepository resumeRepository,
                         ObjectConverter objectConverter, SubpartsModificationUtility modificationUtility,
                         ResumeGetUtility getUtility) {
        this.resumeRepository = resumeRepository;
        this.objectConverter = objectConverter;
        this.modificationUtility = modificationUtility;
        this.getUtility = getUtility;
    }

    @Transactional
    public Header changeHeaderInfo(HeaderForm headerForm, UUID headerID){
        return modificationUtility.modifyHeader(headerForm, headerID);
    }

    @Transactional
    public Resume createNewResumeFor(String resume_name, User user){
        if(user.maxResumeLimitReached()){
            throw new RuntimeException("You have reached the maximum amount of resumes you can have as a free user.");
        }
        else{
            Resume newResume = new Resume(resume_name, user);
            user.getResumes().add(newResume);
            return resumeRepository.save(newResume);

        }
    }

    public Resume findByUserIndex(User user, int index){
        return getUtility.findByUserResumeIndex(user, index);
    }

    @Transactional
    public Experience changeExperienceInfo(ExperienceForm experienceForm, UUID experienceID){
        return modificationUtility.modifyExperience(experienceForm, experienceID);

    }

    @Transactional
    public Education changeEducationInfo(EducationForm educationForm,
                                      UUID educationID){
       return modificationUtility.modifyEducation(educationForm, educationID);
    }

    @Transactional
    public Header createNewHeader(User changingUser, int resumeIndex, HeaderForm headerForm){
        Resume resume = getUtility.findByUserResumeIndex(changingUser, resumeIndex);
        Header newHeader = new Header(headerForm.number(), headerForm.firstName(),
                headerForm.lastName(), headerForm.email());
        resume.setHeader(newHeader);
        resumeRepository.save(resume);
        return newHeader;

    }

    @Transactional
    public Resume createNewExperience(User changingUser, int resumeIndex,
                                                                   ExperienceForm experienceForm,
                                                                   Integer experiencesIndex){
        Resume resume = getUtility.findByUserResumeIndex(changingUser, resumeIndex);
        YearMonth start = YearMonthStringOperations.getYearMonth(experienceForm.startDate());
        YearMonth end = YearMonthStringOperations.getYearMonth(experienceForm.endDate());
        Experience newExperience = new Experience(experienceForm.companyName(),
                experienceForm.technologies(), experienceForm.location(),
                start, end, experienceForm.bullets());

        if (experiencesIndex == null) {
            resume.addExperience(newExperience);
        }
        else {
            resume.addExperience(experiencesIndex, newExperience);
        }
        return resumeRepository.save(resume);



    }

    @Transactional
    public Resume createNewEducation(User changingUser, int resumeIndex, EducationForm educationForm){
        Resume resume = getUtility.findByUserResumeIndex(changingUser, resumeIndex);
        YearMonth startDate = YearMonthStringOperations.getYearMonth(educationForm.startDate());
        YearMonth endDate = YearMonthStringOperations.getYearMonth(educationForm.endDate());
        Education education = new Education(educationForm.schoolName(), educationForm.relevantCoursework(),
                educationForm.location(),
                startDate, endDate);
        resume.setEducation(education);
        return resumeRepository.save(resume);
    }

    @Transactional
    public Resume createNewSection(User user, int resumeIndex,
                                   SectionForm sectionForm, Integer sectionsIndex){
        Resume resume = getUtility.findByUserResumeIndex(user, resumeIndex);
        List<SectionEntry> transformedEntries = objectConverter.
                extractResumeSectionEntries(sectionForm.entryForms());
        Section newSection = new Section(transformedEntries, sectionForm.title());

        if (sectionsIndex == null){
            resume.addSection(newSection);
        }
        else {
            resume.addSection(sectionsIndex, newSection);
        }
        return resumeRepository.save(resume);
    }

    @Transactional
    public void deleteById(UUID id){
        resumeRepository.deleteById(id);
    }

    @Transactional
    public Resume deleteEducation(User changingUser, int resumeIndex){
        Resume resume = getUtility.findByUserResumeIndex(changingUser, resumeIndex);
        resume.setEducation(null);
        return resumeRepository.save(resume);
    }

    @Transactional
    public Resume deleteExperience(User changingUser, int resumeIndex, int experienceIndex){
        Resume resume = getUtility.findByUserResumeIndex(changingUser, resumeIndex);
        resume.getExperiences().remove(experienceIndex);
        return resumeRepository.save(resume);
    }

    @Transactional
    public Resume deleteSection(User user, int resumeIndex, int sectionIndex){
        Resume resume = getUtility.findByUserResumeIndex(user, resumeIndex);
        resume.getSections().remove(sectionIndex);
        return resumeRepository.save(resume);
    }

    @Transactional
    public Resume deleteHeader(User changingUser, int resumeIndex){
        Resume resume = getUtility.findByUserResumeIndex(changingUser, resumeIndex);
        resume.setHeader(null);
        return resumeRepository.save(resume);
    }

    @Transactional
    public Resume setExperiences(Resume resume, List<Experience> newExperiences){
        resume.setExperiences(newExperiences);
        return resumeRepository.save(resume);
    }

    @Transactional
    public Resume setHeader(Resume resume, Header newHeader){
        resume.setHeader(newHeader);
        return resumeRepository.save(resume);
    }

    @Transactional
    public Resume setEducation(Resume resume, Education newEducation){
        resume.setEducation(newEducation);
        return resumeRepository.save(resume);
    }

    @Transactional
    public Resume setSections(Resume resume, List<Section> newSections){
        resume.setSections(newSections);
        return resumeRepository.save(resume);
    }

    @Transactional
    public Resume fullUpdate(Resume resume, FullInformationForm resumeForm) {


        //We can't modify the resume's fields directly here, as that would also modify the variables that
        // we declared outside the try block, causing a bug.
        Header newHeader = new Header(resumeForm.headerForm().number(),
                resumeForm.headerForm().firstName(),
                resumeForm.headerForm().lastName(), resumeForm.headerForm().email());
        resume.setHeader(newHeader);

        Education newEducation = new Education(resumeForm.educationForm().schoolName(),
                resumeForm.educationForm().relevantCoursework(),
                resumeForm.educationForm().location(),
                YearMonthStringOperations.getYearMonth(resumeForm.educationForm().startDate()),
                YearMonthStringOperations.getYearMonth(resumeForm.educationForm().endDate()));
        resume.setEducation(newEducation);
        resume.setExperiences(objectConverter.extractExperiences(resumeForm.experiences(), resume));
        resume.setSections(objectConverter.extractResumeSections(resumeForm.sections(), resume));
        return resumeRepository.save(resume);

    }

    @Transactional
    public Resume changeName(User changingUser, int resumeIndex, String newName){
        Resume changingResume = getUtility.findByUserResumeIndex(changingUser, resumeIndex);
        changingResume.setName(newName);
        return resumeRepository.save(changingResume);

    }

    @Transactional
    public Resume copyResume(User user, int index, String newName){
        Resume copiedResume = getUtility.findByUserResumeIndex(user, index);
        if(newName.equals(copiedResume.getName())){
            throw new RuntimeException("The new resume must have a different name than the original one.");
        }
        Resume newResume = new Resume(copiedResume, newName);
        return resumeRepository.save(newResume);

    }


}

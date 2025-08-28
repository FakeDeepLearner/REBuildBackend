package com.rebuild.backend.service.resume_services;


import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.forms.resume_forms.*;
import com.rebuild.backend.repository.*;

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

    private final ResumeGetUtility getUtility;

    private final HeaderRepository headerRepository;

    private final ExperienceRepository experienceRepository;

    private final SectionRepository sectionRepository;

    private final EducationRepository educationRepository;

    @Autowired
    public ResumeService(ResumeRepository resumeRepository,
                         ObjectConverter objectConverter,
                         ResumeGetUtility getUtility,
                         HeaderRepository headerRepository,
                         ExperienceRepository experienceRepository,
                         SectionRepository sectionRepository, EducationRepository educationRepository) {
        this.resumeRepository = resumeRepository;
        this.objectConverter = objectConverter;
        this.getUtility = getUtility;
        this.headerRepository = headerRepository;
        this.experienceRepository = experienceRepository;
        this.sectionRepository = sectionRepository;
        this.educationRepository = educationRepository;
    }

    @Transactional
    public Header changeHeaderInfo(HeaderForm headerForm, UUID headerID){
        // undoAdder.addUndoResumeState(resID, resume);
        Header header = headerRepository.findById(headerID).orElse(null);
        assert header != null;
        header.setEmail(headerForm.email());
        header.setNumber(headerForm.number());
        header.setFirstName(headerForm.firstName());
        header.setLastName(headerForm.lastName());
        return headerRepository.save(header);
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
        Experience changingExperience = experienceRepository.findById(experienceID).orElse(null);
        assert changingExperience != null;

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
    public Education changeEducationInfo(EducationForm educationForm,
                                      UUID educationID){
        Education education = educationRepository.findById(educationID).orElse(null);
        assert education != null;
        education.setRelevantCoursework(educationForm.relevantCoursework());
        education.setSchoolName(educationForm.schoolName());
        education.setLocation(educationForm.location());
        education.setStartDate(YearMonthStringOperations.getYearMonth(educationForm.startDate()));
        education.setEndDate(YearMonthStringOperations.getYearMonth(educationForm.endDate()));
        return educationRepository.save(education);
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
        List<ResumeSectionEntry> transformedEntries = objectConverter.
                extractResumeSectionEntries(sectionForm.entryForms());
        ResumeSection newSection = new ResumeSection(transformedEntries, sectionForm.title());

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
    public Resume setSections(Resume resume, List<ResumeSection> newSections){
        resume.setSections(newSections);
        return resumeRepository.save(resume);
    }

    @Transactional
    public Resume fullUpdate(Resume resume, FullResumeForm resumeForm) {


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

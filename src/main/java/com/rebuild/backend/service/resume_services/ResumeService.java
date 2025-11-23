package com.rebuild.backend.service.resume_services;


import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.forms.resume_forms.*;

import com.rebuild.backend.repository.resume_repositories.ResumeRepository;
import com.rebuild.backend.repository.resume_repositories.ResumeSearchRepository;
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

    private final ResumeSearchRepository resumeSearchRepository;

    @Autowired
    public ResumeService(ResumeRepository resumeRepository,
                         ObjectConverter objectConverter, SubpartsModificationUtility modificationUtility,
                         ResumeGetUtility getUtility, ResumeSearchRepository resumeSearchRepository) {
        this.resumeRepository = resumeRepository;
        this.objectConverter = objectConverter;
        this.modificationUtility = modificationUtility;
        this.getUtility = getUtility;
        this.resumeSearchRepository = resumeSearchRepository;
    }

    public ResumeSpecsForm createSpecsForm(ResumeSearchConfiguration searchConfiguration)
    {
        return new ResumeSpecsForm(searchConfiguration.getResumeNameSearch(), searchConfiguration.getFirstNameSearch(),
                searchConfiguration.getLastNameSearch(), searchConfiguration.getCreationAfterCutoff(),
                searchConfiguration.getCreationBeforeCutoff(),
                searchConfiguration.getSchoolNameSearch(), searchConfiguration.getCourseworkSearch(),
                searchConfiguration.getCompanySearch(),
                searchConfiguration.getBulletsSearch(), searchConfiguration.getTechnologiesSearch());
    }

    public ResumeSearchConfiguration createSearchConfig(User authenticatedUser,
                                                        ResumeSpecsForm specsForm){
        UserProfile getProfile = authenticatedUser.getProfile();

        ResumeSearchConfiguration newConfiguration = new ResumeSearchConfiguration(specsForm);
        newConfiguration.setAssociatedProfile(getProfile);
        getProfile.addResumeSearchConfig(newConfiguration);
        return resumeSearchRepository.save(newConfiguration);
    }

    @Transactional
    public Header changeHeaderInfo(HeaderForm headerForm, UUID headerID, UUID resumeId, User user){
        return modificationUtility.modifyResumeHeader(headerForm, headerID, resumeId, user);
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

    public Resume findByUserIndex(User user, UUID resumeID){
        return getUtility.findByUserResumeId(user, resumeID);
    }

    @Transactional
    public Experience changeExperienceInfo(ExperienceForm experienceForm, UUID experienceID, UUID resumeId,
                                           User user){
        return modificationUtility.modifyResumeExperience(experienceForm, experienceID, resumeId, user);

    }

    @Transactional
    public Education changeEducationInfo(EducationForm educationForm,
                                      UUID educationID, UUID resumeId, User user){
       return modificationUtility.modifyResumeEducation(educationForm, educationID, resumeId, user);
    }

    @Transactional
    public Header createNewHeader(User changingUser, UUID resumeId, HeaderForm headerForm){
        Resume resume = getUtility.findByUserResumeId(changingUser, resumeId);
        Header newHeader = new Header(headerForm.number(), headerForm.firstName(),
                headerForm.lastName(), headerForm.email());
        resume.setHeader(newHeader);
        newHeader.setResume(resume);
        resumeRepository.save(resume);
        return newHeader;

    }

    @Transactional
    public Resume createNewExperience(User changingUser, UUID resumeId,
                                                                   ExperienceForm experienceForm,
                                                                   Integer experiencesIndex){
        Resume resume = getUtility.findByUserResumeId(changingUser, resumeId);
        YearMonth start = YearMonthStringOperations.getYearMonth(experienceForm.startDate());
        YearMonth end = YearMonthStringOperations.getYearMonth(experienceForm.endDate());
        List<ExperienceType> types = objectConverter.convertToExperienceTypes(experienceForm.experienceTypeValues());
        Experience newExperience = new Experience(experienceForm.companyName(),
                experienceForm.technologies(), experienceForm.location(), types,
                start, end, experienceForm.bullets());
        newExperience.setResume(resume);

        if (experiencesIndex == null) {
            resume.addExperience(newExperience);
        }
        else {
            resume.addExperience(experiencesIndex, newExperience);
        }
        return resumeRepository.save(resume);

    }

    @Transactional
    public Resume createNewEducation(User changingUser, UUID resumeId, EducationForm educationForm){
        Resume resume = getUtility.findByUserResumeId(changingUser, resumeId);
        YearMonth startDate = YearMonthStringOperations.getYearMonth(educationForm.startDate());
        YearMonth endDate = YearMonthStringOperations.getYearMonth(educationForm.endDate());
        Education education = new Education(educationForm.schoolName(), educationForm.relevantCoursework(),
                educationForm.location(),
                startDate, endDate);
        resume.setEducation(education);
        education.setResume(resume);
        return resumeRepository.save(resume);
    }

    @Transactional
    public void deleteById(User deletingUser, UUID id){
        Resume resume = getUtility.findByUserResumeId(deletingUser, id);
        resumeRepository.delete(resume);
    }

    @Transactional
    public Resume deleteEducation(User changingUser, UUID resumeId){
        Resume resume = getUtility.findByUserResumeId(changingUser, resumeId);
        resume.setEducation(null);
        return resumeRepository.save(resume);
    }

    @Transactional
    public Resume deleteExperience(User changingUser, UUID resumeId, int experienceIndex){
        Resume resume = getUtility.findByUserResumeId(changingUser, resumeId);
        resume.getExperiences().remove(experienceIndex);
        return resumeRepository.save(resume);
    }


    @Transactional
    public Resume deleteHeader(User changingUser, UUID resumeId){
        Resume resume = getUtility.findByUserResumeId(changingUser, resumeId);
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
        newHeader.setResume(resume);
        return resumeRepository.save(resume);
    }

    @Transactional
    public Resume setEducation(Resume resume, Education newEducation){
        resume.setEducation(newEducation);
        newEducation.setResume(resume);
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
        newHeader.setResume(resume);

        Education newEducation = new Education(resumeForm.educationForm().schoolName(),
                resumeForm.educationForm().relevantCoursework(),
                resumeForm.educationForm().location(),
                YearMonthStringOperations.getYearMonth(resumeForm.educationForm().startDate()),
                YearMonthStringOperations.getYearMonth(resumeForm.educationForm().endDate()));
        newEducation.setResume(resume);
        resume.setEducation(newEducation);

        resume.setExperiences(objectConverter.extractExperiences(resumeForm.experiences(), resume));
        return resumeRepository.save(resume);

    }

    @Transactional
    public Resume changeName(User changingUser, UUID resumeId, String newName){
        Resume changingResume = getUtility.findByUserResumeId(changingUser, resumeId);
        changingResume.setName(newName);
        return resumeRepository.save(changingResume);

    }

    @Transactional
    public Resume copyResume(User user, UUID resumeId, ResumeCreationForm creationForm){
        Resume copiedResume = getUtility.findByUserResumeId(user, resumeId);
        if(creationForm.newName().equals(copiedResume.getName())){
            throw new RuntimeException("The new resume must have a different name than the original one.");
        }
        Resume newResume = new Resume(copiedResume, creationForm);
        return resumeRepository.save(newResume);

    }


}

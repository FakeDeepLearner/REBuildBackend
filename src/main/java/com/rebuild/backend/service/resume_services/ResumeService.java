package com.rebuild.backend.service.resume_services;


import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.exceptions.PrefillException;
import com.rebuild.backend.model.forms.resume_forms.*;

import com.rebuild.backend.repository.resume_repositories.ResumeRepository;
import com.rebuild.backend.repository.resume_repositories.ResumeSearchRepository;
import com.rebuild.backend.repository.user_repositories.ProfileRepository;
import com.rebuild.backend.service.util_services.SubpartsModificationUtility;
import com.rebuild.backend.utils.ResumeGetUtility;
import com.rebuild.backend.utils.YearMonthStringOperations;
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

    private final SubpartsModificationUtility modificationUtility;

    private final ResumeGetUtility getUtility;

    private final ResumeSearchRepository resumeSearchRepository;
    private final ProfileRepository profileRepository;

    @Autowired
    public ResumeService(ResumeRepository resumeRepository,
                         SubpartsModificationUtility modificationUtility,
                         ResumeGetUtility getUtility, ResumeSearchRepository resumeSearchRepository, ProfileRepository profileRepository) {
        this.resumeRepository = resumeRepository;
        this.modificationUtility = modificationUtility;
        this.getUtility = getUtility;
        this.resumeSearchRepository = resumeSearchRepository;
        this.profileRepository = profileRepository;
    }

    public ResumeSpecsForm createSpecsForm(ResumeSearchConfiguration searchConfiguration)
    {
        return new ResumeSpecsForm(searchConfiguration.getResumeNameSearch(), searchConfiguration.getFirstNameSearch(),
                searchConfiguration.getLastNameSearch(), searchConfiguration.getCreationAfterCutoff().toString(),
                searchConfiguration.getCreationBeforeCutoff().toString(),
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
                                      ExperienceForm experienceForm){
        Resume resume = getUtility.findByUserResumeId(changingUser, resumeId);
        YearMonth start = YearMonthStringOperations.getYearMonth(experienceForm.startDate());
        YearMonth end = YearMonthStringOperations.getYearMonth(experienceForm.endDate());
        Experience newExperience = new Experience(experienceForm.companyName(),
                experienceForm.technologies(), experienceForm.location(), experienceForm.experienceType(),
                start, end, experienceForm.bullets());
        newExperience.setResume(resume);

        resume.addExperience(newExperience);

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
    public void setExperiences(Resume resume, List<Experience> newExperiences){
        newExperiences.forEach(exp -> exp.setResume(resume));
        resume.setExperiences(newExperiences);

    }

    @Transactional
    public void setHeader(Resume resume, Header newHeader){
        Header resumeHeader = resume.getHeader();
        if (resumeHeader == null) {
            resume.setHeader(newHeader);
            newHeader.setResume(resume);
            return;
        }

        modificationUtility.modifyHeaderData(newHeader, resumeHeader);

    }

    @Transactional
    public void setEducation(Resume resume, Education newEducation){
        Education resumeEducation =  resume.getEducation();
        if (resumeEducation == null) {
            resume.setEducation(newEducation);
            newEducation.setResume(resume);
            return;
        }
        modificationUtility.modifyEducationData(newEducation, resumeEducation);

    }

    @Transactional
    public Resume fullUpdate(Resume resume, FullInformationForm resumeForm) {


        //We can't modify the resume's fields directly here, as that would also modify the variables that
        // we declared outside the try block, causing a bug.
        Header newHeader = new Header(resumeForm.headerForm().number(),
                resumeForm.headerForm().firstName(),
                resumeForm.headerForm().lastName(), resumeForm.headerForm().email());
        setHeader(resume, newHeader);

        Education newEducation = new Education(resumeForm.educationForm().schoolName(),
                resumeForm.educationForm().relevantCoursework(),
                resumeForm.educationForm().location(),
                YearMonthStringOperations.getYearMonth(resumeForm.educationForm().startDate()),
                YearMonthStringOperations.getYearMonth(resumeForm.educationForm().endDate()));
        setEducation(resume, newEducation);

        setExperiences(resume, extractExperiences(resumeForm.experiences(), resume));
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


    public Resume prefillHeader(UUID resumeID, User authenticatedUser)
    {
        Resume associatedResume = findByUserIndex(authenticatedUser, resumeID);

        Header header = authenticatedUser.getProfile().getHeader();
        if(header== null){
            throw new PrefillException("Your profile does not have a header set");
        }
        Header newHeader = Header.copy(header);
        setHeader(associatedResume, newHeader);
        return resumeRepository.save(associatedResume);
    }

    public Resume prefillEducation(UUID resumeID, User authenticatedUser)
    {
        Resume associatedResume = findByUserIndex(authenticatedUser, resumeID);

        Education education = authenticatedUser.getProfile().getEducation();
        if(education == null){
            throw new PrefillException("Your profile does not have an education set");
        }
        Education newEducation = Education.copy(education);
        setEducation(associatedResume, newEducation);
        return resumeRepository.save(associatedResume);
    }


    public Resume prefillExperiencesList(UUID resumeID, User authenticatedUser){

        Resume associatedResume = findByUserIndex(authenticatedUser, resumeID);
        List<Experience> experienceList = authenticatedUser.getProfile().getExperienceList();
        if(experienceList == null){
            throw new PrefillException("Your profile does not have experiences set");
        }
        List<Experience> newExperiences = experienceList.
                stream().map(Experience::copy).peek(experience -> {
                    experience.setResume(associatedResume);
                }).
                toList();
        setExperiences(associatedResume, newExperiences);
        return resumeRepository.save(associatedResume);

    }


    private List<Experience> extractExperiences(List<ExperienceForm> experienceForms, Resume associatedResume){
        return experienceForms.stream().map( rawForm -> {
                    Experience newExperience = new Experience(rawForm.companyName(),
                            rawForm.technologies(), rawForm.location(), rawForm.experienceType(),
                            YearMonthStringOperations.getYearMonth(rawForm.startDate()),
                            YearMonthStringOperations.getYearMonth(rawForm.endDate()),
                            rawForm.bullets());
                    newExperience.setResume(associatedResume);
                    return newExperience;
                }
            ).toList();

    }


}

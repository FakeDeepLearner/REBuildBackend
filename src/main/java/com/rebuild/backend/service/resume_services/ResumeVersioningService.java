package com.rebuild.backend.service.resume_services;

import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.entities.versioning_entities.*;
import com.rebuild.backend.model.forms.resume_forms.VersionCreationForm;
import com.rebuild.backend.model.forms.resume_forms.VersionSwitchPreferencesForm;
import com.rebuild.backend.repository.resume_repositories.ResumeRepository;
import com.rebuild.backend.repository.resume_repositories.ResumeVersionRepository;
import com.rebuild.backend.utils.ResumeGetUtility;
import com.rebuild.backend.utils.converters.ObjectConverter;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ResumeVersioningService {

    private final ResumeRepository resumeRepository;

    private final ResumeGetUtility getUtility;

    private final ObjectConverter objectConverter;

    private final ResumeVersionRepository versionRepository;

    private final EntityManager entityManager;

    public ResumeVersioningService(ResumeRepository resumeRepository, ResumeGetUtility getUtility,
                                   ObjectConverter objectConverter,
                                   ResumeVersionRepository versionRepository, EntityManager entityManager) {
        this.resumeRepository = resumeRepository;
        this.getUtility = getUtility;
        this.objectConverter = objectConverter;
        this.versionRepository = versionRepository;
        this.entityManager = entityManager;
    }


    @Transactional
    public ResumeVersion snapshotCurrentData(User user, UUID resumeId, VersionCreationForm inclusionForm){
        Resume copiedResume = getUtility.findByUserResumeId(user, resumeId);
        ResumeVersion newVersion = createSnapshot(copiedResume, inclusionForm);
        int currentVersionCount = copiedResume.getVersionCount();
        if(currentVersionCount < Resume.MAX_VERSION_COUNT){
            copiedResume.setVersionCount(copiedResume.getVersionCount() + 1);
        }
        return versionRepository.save(newVersion);
    }

    @Transactional
    public Resume switchToAnotherVersion(User user,
                                         UUID resumeId, UUID versionId,
                                         VersionSwitchPreferencesForm versionSwitchPreferencesForm){
        Resume switchingResume = getUtility.findByUserResumeId(user, resumeId);

        ResumeVersion versionToSwitch = versionRepository.findById(versionId).orElse(null);
        assert versionToSwitch != null;

        handleVersionSwitch(switchingResume, versionToSwitch, versionSwitchPreferencesForm);
        versionRepository.save(versionToSwitch);
        return resumeRepository.save(switchingResume);

    }

    private void handleVersionSwitch(Resume resume, ResumeVersion versionToSwitch,
                                     VersionSwitchPreferencesForm preferencesForm){
        if(versionToSwitch.getVersionedName() != null){
            String tempName =  resume.getName();
            resume.setName(versionToSwitch.getVersionedName());
            versionToSwitch.setVersionedName(tempName);
        }

        if(versionToSwitch.getVersionedHeader() != null && preferencesForm.includeHeader()){
            Header newHeader = getHeader(resume, versionToSwitch, preferencesForm.makeHeaderCopy());
            newHeader.setResume(resume);
            resume.setHeader(newHeader);
        }

        if(versionToSwitch.getVersionedEducation() != null && preferencesForm.includeEducation()){
            Education newEducation = getEducation(resume, versionToSwitch, preferencesForm.makeEducationCopy());
            newEducation.setResume(resume);
            resume.setEducation(newEducation);
        }

        if(versionToSwitch.getVersionedExperiences() != null && !preferencesForm.experienceIndices().isEmpty()){
            List<Experience> newExperiences = getExperiences(resume, versionToSwitch,
                    preferencesForm.makeExperienceCopies(), preferencesForm.experienceIndices());
            newExperiences.forEach(exp -> exp.setResume(resume));
            resume.setExperiences(newExperiences);
        }

    }

    private static List<Experience> getExperiences(Resume resume, ResumeVersion versionToSwitch,
                                                   boolean makeCopies, List<Integer> indicesToSelect){
        if (!makeCopies) {
            List<Experience> oldResumeExperiences = resume.getExperiences();

            List<Experience> versionedExperiences = versionToSwitch.getVersionedExperiences();
            versionToSwitch.setVersionedExperiences(oldResumeExperiences);
            return versionedExperiences;
        }
        else
        {
            List<Experience> versionedExperiences = versionToSwitch.getVersionedExperiences();

            return indicesToSelect.stream()
                    .map(versionedExperiences::get)
                    .map(Experience::copy).toList();
        }
    }

    private static Education getEducation(Resume resume, ResumeVersion versionToSwitch, boolean makeCopy) {
        if (!makeCopy) {

            Education oldResumeEducation = resume.getEducation();

            Education versionedEducation = versionToSwitch.getVersionedEducation();

            versionToSwitch.setVersionedEducation(oldResumeEducation);
            return versionedEducation;
        }
        else
        {
            return Education.copy(versionToSwitch.getVersionedEducation());
        }
    }

    private static Header getHeader(Resume resume, ResumeVersion versionToSwitch, boolean makeCopy) {
        if (!makeCopy) {
            Header oldResumeHeader = resume.getHeader();

            Header versionedHeader = versionToSwitch.getVersionedHeader();

            versionToSwitch.setVersionedHeader(oldResumeHeader);

            return versionedHeader;
        }
        else
        {
            return Header.copy(versionToSwitch.getVersionedHeader());

        }
    }


    private ResumeVersion createSnapshot(Resume resume, VersionCreationForm inclusionForm){
        ResumeVersion newVersion = new ResumeVersion();
        objectConverter.createVersionedHeader(resume.getHeader(),
                inclusionForm.includeHeader(), newVersion);
        objectConverter.createVersionedEducation(
                resume.getEducation(), inclusionForm.includeEducation(), newVersion
        );
        objectConverter.createVersionedExperiences(
                resume.getExperiences(), inclusionForm.includeExperience(), newVersion
        );
        String versionedName = inclusionForm.includeName() ? resume.getName() : null;
        newVersion.setVersionedName(versionedName);
        newVersion.setAssociatedResume(resume);
        resume.getVersions().add(newVersion);
        return newVersion;
    }


    @Transactional
    public void deleteVersion(User user, UUID resumeId, UUID versionId){
        Resume deletingResume = getUtility.findByUserResumeId(user, resumeId);
        deletingResume.setVersionCount(deletingResume.getVersionCount() - 1);
        ResumeVersion versionToDelete = versionRepository.findById(versionId).orElse(null);
        assert versionToDelete != null;
        versionRepository.delete(versionToDelete);
        resumeRepository.save(deletingResume);
    }
}

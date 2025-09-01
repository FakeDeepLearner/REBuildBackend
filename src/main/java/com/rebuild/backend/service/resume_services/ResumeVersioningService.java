package com.rebuild.backend.service.resume_services;

import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.entities.versioning_entities.*;
import com.rebuild.backend.model.forms.resume_forms.VersionInclusionForm;
import com.rebuild.backend.model.forms.resume_forms.VersionSwitchPreferencesForm;
import com.rebuild.backend.repository.ResumeRepository;
import com.rebuild.backend.repository.ResumeVersionRepository;
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

    private List<ResumeVersion> findVersionsByIdAndLimit(UUID resumeId, int limit){
        return entityManager.createNamedQuery("ResumeVersion.findAllByResumeIdWithLimit", ResumeVersion.class).
                setParameter("resumeId", resumeId).setMaxResults(limit).getResultList();
    }


    @Transactional
    public ResumeVersion snapshotCurrentData(User user, int index, VersionInclusionForm inclusionForm){
        Resume copiedResume = getUtility.findByUserResumeIndex(user, index);
        ResumeVersion newVersion = createSnapshot(copiedResume, inclusionForm);
        int currentVersionCount = copiedResume.getVersionCount();
        if(currentVersionCount < Resume.MAX_VERSION_COUNT){
            copiedResume.setVersionCount(copiedResume.getVersionCount() + 1);
        }
        return versionRepository.save(newVersion);
    }

    @Transactional
    public Resume switchToAnotherVersion(User user,
                                         int resume_index, int version_index,
                                         VersionSwitchPreferencesForm versionSwitchPreferencesForm){
        Resume switchingResume = getUtility.findByUserResumeIndex(user, resume_index);

        ResumeVersion versionToSwitch = findVersionsByIdAndLimit(switchingResume.getId(), version_index).getLast();
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
            resume.setHeader(newHeader);
        }

        if(versionToSwitch.getVersionedEducation() != null && preferencesForm.includeEducation()){
            Education newEducation = getEducation(resume, versionToSwitch, preferencesForm.makeEducationCopy());
            resume.setEducation(newEducation);
        }

        if(versionToSwitch.getVersionedExperiences() != null && !preferencesForm.experienceIndices().isEmpty()){
            List<Experience> newExperiences = getExperiences(resume, versionToSwitch,
                    preferencesForm.makeExperienceCopies(), preferencesForm.experienceIndices());
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


    private ResumeVersion createSnapshot(Resume resume, VersionInclusionForm inclusionForm){
        ResumeVersion newVersion = new ResumeVersion();
        Header newHeader = objectConverter.createVersionedHeader(resume.getHeader(),
                inclusionForm.includeHeader(), newVersion);
        Education newEducation = objectConverter.createVersionedEducation(
                resume.getEducation(), inclusionForm.includeEducation(), newVersion
        );
        List<Experience> experiences = objectConverter.createVersionedExperiences(
                resume.getExperiences(), inclusionForm.includeExperience(), newVersion
        );
        String versionedName = inclusionForm.includeName() ? resume.getName() : null;
        newVersion.setVersionedName(versionedName);
        newVersion.setVersionedHeader(newHeader);
        newVersion.setVersionedEducation(newEducation);
        newVersion.setVersionedExperiences(experiences);
        newVersion.setAssociatedResume(resume);
        return newVersion;
    }


    @Transactional
    public void deleteVersion(User user, int resumeIndex, int versionIndex){
        Resume deletingResume = getUtility.findByUserResumeIndex(user, resumeIndex);
        deletingResume.setVersionCount(deletingResume.getVersionCount() - 1);
        ResumeVersion versionToDelete = findVersionsByIdAndLimit(deletingResume.getId(), versionIndex).getLast();
        versionRepository.delete(versionToDelete);
        resumeRepository.save(deletingResume);
    }
}

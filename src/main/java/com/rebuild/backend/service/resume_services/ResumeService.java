package com.rebuild.backend.service.resume_services;


import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.utils.BulletsUtil;
import com.rebuild.backend.utils.exceptions.ApiException;
import com.rebuild.backend.model.forms.resume_forms.*;

import com.rebuild.backend.model.responses.resume_responses.*;
import com.rebuild.backend.repository.resume_repositories.ExperienceRepository;
import com.rebuild.backend.repository.resume_repositories.ProjectRepository;
import com.rebuild.backend.repository.resume_repositories.ResumeRepository;
import com.rebuild.backend.utils.StringUtil;
import com.rebuild.backend.utils.exceptions.BelongingException;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ResumeService {

    private final ResumeRepository resumeRepository;

    private final ResumeObtainer getUtility;

    @Autowired
    public ResumeService(ResumeRepository resumeRepository,
                         ResumeObtainer getUtility) {
        this.resumeRepository = resumeRepository;
        this.getUtility = getUtility;
    }

    @Transactional
    public ResumeResponse createNewResumeFor(String resume_name, User user){
        Optional<Resume> foundResume = resumeRepository.findByUserAndName(user, resume_name);
        if (foundResume.isPresent())
        {
            throw new ApiException(HttpStatus.CONFLICT, "You already have a resume with this name");
        }

        Resume newResume = new Resume(resume_name, user);
        user.getResumes().add(newResume);
        return resumeRepository.save(newResume).toResponse();

    }

    public ResumeResponse findByUserAndResumeId(User user, UUID resumeID){
        Resume foundResume = getUtility.findByUserResumeId(user, resumeID);
        return foundResume.toResponse();
    }

    @Transactional
    public Resume changeName(User changingUser, UUID resumeId, String newName){
        Optional<Resume> foundResume = resumeRepository.findByUserAndName(changingUser, newName);
        if (foundResume.isPresent())
        {
            throw new ApiException(HttpStatus.CONFLICT, "You already have a resume with this name");
        }
        Resume changingResume = getUtility.findByUserResumeId(changingUser, resumeId);
        changingResume.setName(newName);
        return resumeRepository.save(changingResume);

    }

    @Transactional
    public Resume copyResume(User user, UUID resumeId, String name){

        Optional<Resume> foundResume = resumeRepository.findByUserAndName(user, name);
        if (foundResume.isPresent())
        {
            throw new ApiException(HttpStatus.CONFLICT, "You already have a resume with this name");
        }

        Resume copiedResume = getUtility.findByUserAndIdWithAllInfo(user, resumeId);

        Resume newResume = new Resume(copiedResume, name);
        return resumeRepository.save(newResume);

    }

}

package com.rebuild.backend.service;


import com.rebuild.backend.model.entities.Education;
import com.rebuild.backend.model.entities.Experience;
import com.rebuild.backend.model.entities.Header;
import com.rebuild.backend.model.entities.PhoneNumber;
import com.rebuild.backend.repository.ResumeRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ResumeService {

    private final ResumeRepository repository;

    @Autowired
    public ResumeService(ResumeRepository repository) {
        this.repository = repository;
    }


    public Header changeHeaderInfo(UUID resID, String newName, String newEmail, PhoneNumber newPhoneNumber){
        return repository.changeHeaderInfo(resID, newName, newEmail, newPhoneNumber);
    }

    public Experience changeExperienceInfo(UUID resID, UUID expID,
                                           String newCompanyName, String newDuration,
                                           List<String> newBullets){
        return repository.changeExperienceInfo(resID, expID, newCompanyName, newDuration, newBullets);
    }

    public Education changeEducationInfo(UUID resID, String newSchoolName, List<String> newCourseWork){
        return repository.changeEducationInfo(resID, newSchoolName, newCourseWork);
    }






}

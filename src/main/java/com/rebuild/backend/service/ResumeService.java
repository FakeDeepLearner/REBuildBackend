package com.rebuild.backend.service;


import com.rebuild.backend.model.entities.PhoneNumber;
import com.rebuild.backend.repository.ResumeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
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


    public void changeHeaderInfo(UUID resID, String newName, String newEmail, PhoneNumber newPhoneNumber){
        repository.changeHeaderInfo(resID, newName, newEmail, newPhoneNumber);
    }

    public void changeExperienceInfo(UUID resID, UUID expID,
                              String newCompanyName, String newDuration,
                              List<String> newBullets){
        repository.changeExperienceInfo(resID, expID, newCompanyName, newDuration, newBullets);
    }

    public void changeEducationInfo(UUID resID, String newSchoolName, List<String> newCourseWork){
        repository.changeEducationInfo(resID, newSchoolName, newCourseWork);
    }


}

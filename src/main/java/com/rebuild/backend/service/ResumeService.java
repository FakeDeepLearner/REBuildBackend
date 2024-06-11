package com.rebuild.backend.service;


import com.rebuild.backend.model.entities.*;
import com.rebuild.backend.repository.ResumeRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.time.Duration;
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
                                           String newCompanyName,
                                           List<String> newTechnologies,
                                           Duration newDuration,
                                           List<String> newBullets){
        return repository.changeExperienceInfo(resID, expID, newCompanyName, newTechnologies ,newDuration, newBullets);
    }

    public Education changeEducationInfo(UUID resID, String newSchoolName, List<String> newCourseWork){
        return repository.changeEducationInfo(resID, newSchoolName, newCourseWork);
    }

    public Header createNewHeader(UUID resID, String name, String email, PhoneNumber phoneNumber){
        return repository.createNewHeader(resID, name, email,
                phoneNumber.getCountryCode(), phoneNumber.getAreaCode(), phoneNumber.getRestOfNumber());
    }

    public Experience createNewExperience(UUID resID, String companyName,
                                          List<String> technologies,
                                          Duration duration, List<String> bullets){
        return repository.createNewExperience(resID, companyName, technologies, duration, bullets);
    }

    public Education createNewEducation(UUID resID, String schoolName, List<String> courseWork){
        return repository.createNewEducation(resID, schoolName, courseWork);
    }

    public Resume save(Resume resume){
        return repository.save(resume);
    }






}

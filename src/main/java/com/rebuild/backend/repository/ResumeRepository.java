package com.rebuild.backend.repository;

import com.rebuild.backend.model.entities.*;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional
public interface ResumeRepository extends CrudRepository<Resume, UUID> {

    @Modifying
    @Query("UPDATE Resume r SET r.header.name =:newName, " +
            "r.header.number=:newPhoneNumber, " +
            "r.header.email =:newEmail WHERE r.id=:resID")
    Header changeHeaderInfo(UUID resID, String newName, String newEmail, PhoneNumber newPhoneNumber);

    @Modifying
    @Query("UPDATE Experience exp SET exp.companyName=:newCompanyName, exp.timePeriod=:newDuration, " +
            "exp.bullets=:newBullets WHERE exp.id=:expID AND exp.resume.id=:resID")
    Experience changeExperienceInfo(UUID resID, UUID expID,
                                    String newCompanyName, String newDuration,
                                    List<String> newBullets);

    @Modifying
    @Query("UPDATE Resume r SET r.education.schoolName=:newSchoolName, " +
            "r.education.relevantCoursework=:newCourseWork WHERE r.id=:resID")
    Education changeEducationInfo(UUID resID, String newSchoolName, List<String> newCourseWork);


    @Modifying
    @Query(value = "INSERT INTO headers (countryCode, areaCode, restOfNumber, name, email, resume_id)" +
            "VALUES (:countryCode, :areaCode, :restOfNumber, :name, :email, :resID)", nativeQuery = true)
    Header createNewHeader(UUID resID, String name, String email,
                           String countryCode, String areaCode, String restOfNumber);

    @Modifying
    @Query(value = "INSERT INTO experiences (companyName, timePeriod, bullets, resume_id)" +
            "VALUES (:companyName, :timePeriod, :bullets, :resID)", nativeQuery = true)
    Experience createNewExperience(UUID resID, String companyName, String timePeriod, List<String> bullets);

    @Modifying
    @Query(nativeQuery = true, value = "INSERT INTO educations (schoolName, relevantCoursework, resume_id)" +
            "VALUES (:schoolName, :coursework, :resID)")
    Education createNewEducation(UUID resID, String schoolName, List<String> coursework);



}

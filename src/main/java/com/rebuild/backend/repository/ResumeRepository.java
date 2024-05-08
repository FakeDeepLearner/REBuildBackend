package com.rebuild.backend.repository;

import com.rebuild.backend.model.entities.PhoneNumber;
import com.rebuild.backend.model.entities.Resume;
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
    void changeHeaderInfo(UUID resID, String newName, String newEmail, PhoneNumber newPhoneNumber);

    @Modifying
    @Query("UPDATE Experience exp SET exp.companyName=:newCompanyName, exp.timePeriod=:newDuration, " +
            "exp.bullets=:newBullets WHERE exp.id=:expID AND exp.resume.id=:resID")
    void changeExperienceInfo(UUID resID, UUID expID,
                              String newCompanyName, String newDuration,
                              List<String> newBullets);

    @Modifying
    @Query("UPDATE Resume r SET r.education.schoolName=:newSchoolName, " +
            "r.education.relevantCoursework=:newCourseWork WHERE r.id=:resID")
    void changeEducationInfo(UUID resID, String newSchoolName, List<String> newCourseWork);
}

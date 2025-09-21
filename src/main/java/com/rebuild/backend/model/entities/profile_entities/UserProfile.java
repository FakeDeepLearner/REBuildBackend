package com.rebuild.backend.model.entities.profile_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.entities.users.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

import static jakarta.persistence.CascadeType.*;


@Data
@RequiredArgsConstructor
//@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "profiles", indexes = {
        @Index(columnList = "header_id"),
        @Index(columnList = "education_id"),
        @Index(columnList = "experience_id"),
        @Index(columnList = "user_id"),
        @Index(columnList = "picture_id")
})
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JsonIgnore
    private UUID id;

    @OneToOne(fetch = FetchType.EAGER, orphanRemoval = true, cascade = {
            ALL
    })
    @JoinColumn(name = "header_id", referencedColumnName = "id")
    private Header header;

    @OneToOne(fetch = FetchType.EAGER, orphanRemoval = true, cascade = ALL)
    @JoinColumn(name = "education_id", referencedColumnName = "id")
    private Education education;

    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = ALL)
    @JoinColumn(name = "experience_id", referencedColumnName = "id")
    private List<Experience> experienceList;

    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true, cascade = ALL)
    @JoinColumn(name = "picture_id", referencedColumnName = "id")
    private ProfilePicture profilePicture;

    @Column(name = "page_size")
    private int forumPageSize = 20;

    @OneToOne(fetch = FetchType.LAZY, cascade = {
            PERSIST,
            MERGE,
            REFRESH
    })
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @JsonIgnore
    private User user;

    public UserProfile(Header profileHeader,
                       Education newEducation,
                       List<Experience> experiences) {
        this.header = profileHeader;
        this.education = newEducation;
        this.experienceList = experiences;
    }


    public static UserProfile deepCopy(UserProfile originalProfile){
        Header oldHeader = originalProfile.getHeader();
        Header newHeader = new Header(oldHeader.getNumber(), oldHeader.getFirstName(), oldHeader.getLastName(),
                oldHeader.getEmail());

        Education oldEducation = originalProfile.getEducation();

        Education newEducation = new Education(oldEducation.getSchoolName(),
                oldEducation.getRelevantCoursework(), oldEducation.getLocation(), oldEducation.getStartDate(),
                oldEducation.getEndDate());

        List<Experience> newExperiences = originalProfile.getExperienceList().stream().map(
                oldExperience -> new Experience(oldExperience.getCompanyName(),
                        oldExperience.getTechnologyList(), oldExperience.getLocation(),
                        oldExperience.getExperienceTypes(), oldExperience.getStartDate(),
                        oldExperience.getEndDate(), oldExperience.getBullets())
        ).toList();

        return new UserProfile(newHeader, newEducation, newExperiences);
    }


}
